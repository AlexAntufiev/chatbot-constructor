package chat.tamtam.bot.service;

import java.nio.ByteBuffer;
import java.util.NoSuchElementException;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

import chat.tamtam.bot.domain.bot.BotScheme;
import chat.tamtam.bot.domain.bot.TamBotEntity;
import chat.tamtam.bot.domain.builder.component.ComponentType;
import chat.tamtam.bot.domain.builder.component.SchemeComponent;
import chat.tamtam.bot.domain.webhook.BotContext;
import chat.tamtam.bot.repository.BotContextRepository;
import chat.tamtam.bot.repository.BotSchemeRepository;
import chat.tamtam.bot.repository.ComponentRepository;
import chat.tamtam.bot.repository.TamBotRepository;
import chat.tamtam.botapi.TamTamBotAPI;
import chat.tamtam.botapi.model.BotAddedToChatUpdate;
import chat.tamtam.botapi.model.BotRemovedFromChatUpdate;
import chat.tamtam.botapi.model.BotStartedUpdate;
import chat.tamtam.botapi.model.ChatTitleChangedUpdate;
import chat.tamtam.botapi.model.MessageCallbackUpdate;
import chat.tamtam.botapi.model.MessageCreatedUpdate;
import chat.tamtam.botapi.model.MessageEditedUpdate;
import chat.tamtam.botapi.model.MessageRemovedUpdate;
import chat.tamtam.botapi.model.MessageRestoredUpdate;
import chat.tamtam.botapi.model.Update;
import chat.tamtam.botapi.model.UserAddedToChatUpdate;
import chat.tamtam.botapi.model.UserRemovedFromChatUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@RequiredArgsConstructor
public class WebHookBotService {
    private final BotContextRepository botContextRepository;
    private final BotSchemeRepository botSchemeRepository;
    private final TamBotRepository tamBotRepository;
    private final ComponentRepository componentRepository;

    private final ComponentProcessorService componentProcessorService;

    private final HazelcastInstance hazelcastInstance;
    private IMap<Byte[], Object> botContextLockMap;
    private static final String BOT_CONTEXT_LOCK_MAP = "bot-context-lock-map";

    @PostConstruct
    public void initLockMap() {
        botContextLockMap = hazelcastInstance.getMap(BOT_CONTEXT_LOCK_MAP);
    }

    public void submit(final int botSchemeId, final Update update) {
        try {
            log.info(String.format("Update {%s} submitted to bot scheme with id=%d", update, botSchemeId));
            update.visit(new WebHookBotVisitor(botSchemeId));
        } catch (RuntimeException e) {
            log.error("Submitting message produced exception", e);
        }
    }

    @RequiredArgsConstructor
    private class WebHookBotVisitor implements Update.Visitor {
        private final int botSchemeId;

        private Byte[] botContextLockKey;

        private void execute(final BotContext context, final Update update) {
            if (context.getState() == null) {
                log.info(
                        String.format(
                                "%s has state==null, ignoring this update(%s), because bot is locked for this user",
                                context, update
                        )
                );
                return;
            }

            SchemeComponent component =
                    componentRepository
                            .findById(context.getState())
                            .orElseThrow(
                                    () -> new NoSuchElementException(
                                            "Can't find builderComponent with id=" + context.getState()
                                    )
                            );

            TamTamBotAPI api = getTamTamBotAPI(component);

            while (true) {
                try {
                    switch (ComponentType.getById(component.getType())) {
                        case INPUT:
                            if (update instanceof MessageCreatedUpdate) {
                                componentProcessorService
                                        .process(((MessageCreatedUpdate) update), context, component, api);
                            }
                            if (update instanceof MessageCallbackUpdate) {
                                componentProcessorService
                                        .process(((MessageCallbackUpdate) update), context, component, api);
                            }
                            botContextRepository.save(context);
                            break;

                        case INFO:
                            componentProcessorService
                                    .process(context, component, api);
                            botContextRepository.save(context);
                            break;

                        default:
                            break;
                    }
                } catch (RuntimeException e) {
                    log.error(
                            String.format(
                                    "Graph execution produced exception(component=%s, context=%s, update=%s)"
                                    + ", context should be reset on next update",
                                    component, context, update
                            ),
                            e
                    );
                    context.setSchemeUpdate(null);
                    context.setState(null);
                    botContextRepository.save(context);
                    break;
                }

                if (context.getState() == null) {
                    // In this case all further updates will be ignored
                    log.info(String.format("%s has state==null, bot is locked for this user", context, update));
                    break;
                }


                // Reset component ignored for a while
                /*if (context.getState().equals(context.getResetState())) {
                    // In this case reset execution to start state and hang on for further update
                    componentProcessorService.updatePendingMessage(context, api);
                    initContext(context.getId().getUserId());
                    break;
                }*/

                component =
                        componentRepository
                                .findById(context.getState())
                                .orElseThrow(
                                        () -> new NoSuchElementException(
                                                "Can't find next builderComponent with id=" + context.getState()
                                        )
                                );
                if (ComponentType.getById(component.getType()) == ComponentType.INPUT) {
                    break;
                }
            }
        }

        private @NotNull TamTamBotAPI getTamTamBotAPI(SchemeComponent component) {
            BotScheme botScheme =
                    botSchemeRepository
                            .findById(component.getSchemeId())
                            .orElseThrow(
                                    () -> new NoSuchElementException(
                                            "Can't find botScheme with id=" + component.getSchemeId()
                                    )
                            );
            TamBotEntity tamBot =
                    Optional.ofNullable(tamBotRepository.findById(
                            new TamBotEntity.Id(botScheme.getBotId(), botScheme.getUserId()))
                    ).orElseThrow(
                            () -> new NoSuchElementException(
                                    String.format(
                                            "Can't find tam bot with id=%d and userId=%d",
                                            botSchemeId,
                                            botScheme.getBotId()
                                    )
                            )
                    );
            return TamTamBotAPI.create(tamBot.getToken());
        }

        private BotContext getContext(final long userId) {
            BotScheme botScheme = botSchemeRepository
                    .findById(botSchemeId)
                    .orElseThrow(
                            () -> new NoSuchElementException("Can't find bot scheme with id=" + botSchemeId)
                    );
            return botContextRepository
                    .findByIdUserIdAndIdBotSchemeId(userId, botSchemeId)
                    .filter(context -> botScheme.getUpdate().equals(context.getSchemeUpdate()))
                    .orElseGet(() -> initContext(userId));
        }

        private void rejectIfUpdateFromChat(Long userId, Update model) {
            if (userId == null) {
                log.info(String.format("Update from chat - ignoring(%s, botSchemeId=%d)", model, botSchemeId));
                return;
            }
        }

        @Override
        public void visit(final MessageCreatedUpdate model) {
            rejectIfUpdateFromChat(model.getMessage().getRecipient().getUserId(), model);
            log.info("WEB_HOOK_BOT MESSAGE {}", botSchemeId);
            setBotContextLockKey(model.getMessage().getSender().getUserId());
            lock();

            try {
                execute(getContext(model.getMessage().getSender().getUserId()), model);
            } finally {
                unlock();
            }
        }

        @Override
        public void visit(MessageCallbackUpdate model) {
            log.info("WEB_HOOK_BOT CALLBACK {}", botSchemeId);
            setBotContextLockKey(model.getCallback().getUser().getUserId());
            lock();

            try {
                execute(getContext(model.getCallback().getUser().getUserId()), model);
            } finally {
                unlock();
            }
        }

        @Override
        public void visit(MessageEditedUpdate model) {

        }

        @Override
        public void visit(MessageRemovedUpdate model) {

        }

        @Override
        public void visit(MessageRestoredUpdate model) {

        }

        @Override
        public void visit(BotAddedToChatUpdate model) {

        }

        @Override
        public void visit(BotRemovedFromChatUpdate model) {

        }

        @Override
        public void visit(UserAddedToChatUpdate model) {

        }

        @Override
        public void visit(UserRemovedFromChatUpdate model) {

        }

        @Override
        public void visit(BotStartedUpdate model) {
            log.info("WEB_HOOK_BOT STARTED {}", botSchemeId);
            setBotContextLockKey(model.getUserId());
            lock();
            try {
                execute(initContext(model.getUserId()), model);
            } finally {
                unlock();
            }
        }

        private BotContext initContext(final long userId) {
            BotScheme botScheme =
                    botSchemeRepository.findById(botSchemeId)
                            .orElseThrow(
                                    () -> new NoSuchElementException("Can't find bot scheme with id=" + botSchemeId)
                            );

            BotContext context = new BotContext();

            context.setId(new BotContext.Id(userId, botSchemeId));
            context.setState(botScheme.getSchemeEnterState());
            context.setSchemeUpdate(botScheme.getUpdate());
//            For a while
//            context.setResetState(botScheme.getSchemeResetState());

            return botContextRepository.save(context);
        }

        @Override
        public void visit(ChatTitleChangedUpdate model) {

        }

        @Override
        public void visitDefault(Update model) {

        }

        private void lock() {
            botContextLockMap.lock(botContextLockKey);
        }

        // must be called in finally section
        private void unlock() {
            botContextLockMap.unlock(botContextLockKey);
        }

        private void setBotContextLockKey(final long userId) {
            byte[] bytes =
                    ByteBuffer
                            .allocate(Long.BYTES + Integer.BYTES)
                            .putLong(userId)
                            .putInt(botSchemeId)
                            .array();
            botContextLockKey = ArrayUtils.toObject(bytes);
        }
    }
}
