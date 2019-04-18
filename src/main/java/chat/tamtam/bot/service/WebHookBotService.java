package chat.tamtam.bot.service;

import java.util.NoSuchElementException;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import chat.tamtam.bot.domain.bot.BotSchemeEntity;
import chat.tamtam.bot.domain.bot.TamBotEntity;
import chat.tamtam.bot.domain.builder.component.Component;
import chat.tamtam.bot.domain.builder.component.ComponentType;
import chat.tamtam.bot.domain.builder.component.wrapper.InfoComponentWrapper;
import chat.tamtam.bot.domain.builder.validator.Validator;
import chat.tamtam.bot.domain.builder.validator.ValidatorType;
import chat.tamtam.bot.domain.builder.validator.wrapper.EqualTextValidatorWrapper;
import chat.tamtam.bot.domain.webhook.BotContext;
import chat.tamtam.bot.repository.BotContextRepository;
import chat.tamtam.bot.repository.BotSchemeRepository;
import chat.tamtam.bot.repository.ComponentRepository;
import chat.tamtam.bot.repository.ComponentValidatorRepository;
import chat.tamtam.bot.repository.TamBotRepository;
import chat.tamtam.botapi.TamTamBotAPI;
import chat.tamtam.botapi.exceptions.APIException;
import chat.tamtam.botapi.exceptions.ClientException;
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
    private final BotSchemeRepository botSchemaRepository;
    private final TamBotRepository tamBotRepository;
    private final ComponentRepository componentRepository;
    private final ComponentValidatorRepository validatorRepository;

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
        // @todo @CC-141 Implement kind of lock that depends on userId and botId
        private final int botSchemeId;

        private void execute(final BotContext context, final Update update) {
            Component component =
                    componentRepository
                            .findById(context.getState())
                            .orElseThrow(
                                    () -> new NoSuchElementException(
                                            "Can't find component with id=" + context.getState()
                                    )
                            );

            TamTamBotAPI tamTamBotAPI = getTamTamBotAPI(component);

            while (true) {
                switch (ComponentType.getById(component.getType())) {
                    case INPUT:
                        if (!(update instanceof MessageCreatedUpdate)) {
                            break;
                        }
                        processInputComponent(component, context, ((MessageCreatedUpdate) update));
                        System.out.println(context);
                        botContextRepository.save(context);
                        break;

                    case INFO:
                        sendMessage(tamTamBotAPI, new InfoComponentWrapper(component), context.getId().getUserId());
                        context.setState(component.getNextComponent());
                        botContextRepository.save(context);
                        break;

                    default:
                        break;
                }

                if (context.getState() == null) {
                    BotContext botContext = initContext(context.getId().getUserId());
                    break;
                }

                component =
                        componentRepository
                                .findById(context.getState())
                                .orElseThrow(
                                        () -> new NoSuchElementException(
                                                "Can't find next component with id=" + context.getState()
                                        )
                                );
                if (ComponentType.getById(component.getType()) == ComponentType.INPUT) {
                    break;
                }
            }
        }

        private void processInputComponent(
                final Component component,
                final BotContext context,
                final MessageCreatedUpdate update
        ) {
            Iterable<Validator> validators = validatorRepository.findAllByComponentId(component.getId());
            for (Validator validator
                    : validators) {
                switch (ValidatorType.getById(validator.getType())) {
                    case EQUAL_TEXT:
                        EqualTextValidatorWrapper validatorWrapper = new EqualTextValidatorWrapper(validator);
                        if (!validatorWrapper.validate(update)) {
                            context.setState(validator.getFailState());
                            return;
                        }
                        break;
                    default:
                        break;
                }
            }
            context.setState(component.getNextComponent());

            // @todo #CC-141 Invocation of predicates etc.
        }

        private void sendMessage(
                final TamTamBotAPI tamTamBotAPI,
                final InfoComponentWrapper componentWrapper,
                final long recipient
        ) {
            try {
                tamTamBotAPI
                        .sendMessage(componentWrapper.getMessageBody())
                        .userId(recipient)
                        .execute();
            } catch (APIException | ClientException e) {
                log.error("Message sending produced exception", e);
            }
        }

        private @NotNull TamTamBotAPI getTamTamBotAPI(Component component) {
            BotSchemeEntity botScheme =
                    botSchemaRepository
                            .findById(component.getSchemeId())
                            .orElseThrow(
                                    () -> new NoSuchElementException(
                                            "Can't find botScheme with id=" + component.getSchemeId()
                                    )
                            );
            TamBotEntity tamBot =
                    Optional.of(tamBotRepository.findById(
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

        @Override
        public void visit(final MessageCreatedUpdate model) {
            long userId = model.getMessage().getSender().getUserId();
            BotContext context = botContextRepository
                    .findByIdUserIdAndIdBotSchemeId(userId, botSchemeId)
                    .orElseGet(() -> initContext(userId));
            if (context.getState() == null) {
                throw new IllegalStateException(
                        String.format("Context state is null(userId=%d, botSchemeId=%d)", userId, botSchemeId)
                );
            }
            execute(context, model);
        }

        @Override
        public void visit(MessageCallbackUpdate model) {

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
            execute(initContext(model.getUserId()), model);
        }

        private BotContext initContext(final long userId) {
            BotSchemeEntity botScheme =
                    botSchemaRepository.findById(botSchemeId)
                            .orElseThrow(
                                    () -> new NoSuchElementException("Can't find bot scheme with id=" + botSchemeId)
                            );

            BotContext context = new BotContext();

            context.setId(new BotContext.Id(userId, botSchemeId));
            context.setState(botScheme.getSchema());

            return botContextRepository.save(context);
        }

        @Override
        public void visit(ChatTitleChangedUpdate model) {

        }

        @Override
        public void visitDefault(Update model) {

        }
    }
}
