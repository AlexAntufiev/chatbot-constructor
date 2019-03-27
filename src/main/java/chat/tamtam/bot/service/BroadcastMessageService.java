package chat.tamtam.bot.service;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import chat.tamtam.bot.domain.bot.BotSchemeEntity;
import chat.tamtam.bot.domain.bot.TamBotEntity;
import chat.tamtam.bot.domain.broadcast.message.BroadcastMessageEntity;
import chat.tamtam.bot.domain.broadcast.message.BroadcastMessageState;
import chat.tamtam.bot.domain.broadcast.message.BroadcastMessageUpdate;
import chat.tamtam.bot.domain.broadcast.message.action.CreatedStateAction;
import chat.tamtam.bot.domain.broadcast.message.action.DiscardedEraseByUserStateAction;
import chat.tamtam.bot.domain.broadcast.message.action.ScheduledStateAction;
import chat.tamtam.bot.domain.broadcast.message.action.SentStateAction;
import chat.tamtam.bot.domain.chatchannel.ChatChannelEntity;
import chat.tamtam.bot.domain.exception.BroadcastMessageIllegalStateException;
import chat.tamtam.bot.domain.exception.CreateBroadcastMessageException;
import chat.tamtam.bot.domain.exception.NotFoundEntityException;
import chat.tamtam.bot.domain.exception.UpdateBroadcastMessageException;
import chat.tamtam.bot.domain.response.SuccessResponse;
import chat.tamtam.bot.domain.response.SuccessResponseWrapper;
import chat.tamtam.bot.repository.BroadcastMessageRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BroadcastMessageService {
    private static final String ZONED_DATE_TIME_PATTERN = "EEE MMM dd uuuu HH:mm:ss 'GMT'x";
    private final BroadcastMessageRepository broadcastMessageRepository;
    private final BotSchemeService botSchemeService;
    private final TamBotService tamBotService;
    private final ChatChannelService chatChannelService;
    private static final List<Byte> EXCLUDED_STATES =
            Collections.singletonList(BroadcastMessageState.DELETED.getValue());

    // Broadcast message actions
    private final CreatedStateAction createdStateAction;
    private final DiscardedEraseByUserStateAction discardedEraseByUserStateAction;
    private final ScheduledStateAction scheduledStateAction;
    private final SentStateAction sentStateAction;

    public BroadcastMessageEntity getBroadcastMessage(
            final BotSchemeEntity botScheme,
            final TamBotEntity tamBot,
            final Long chatChannelId,
            final Long broadcastMessageId
    ) {
        ChatChannelEntity chatChannel = chatChannelService.getChatChannel(botScheme, tamBot, chatChannelId);
        BroadcastMessageEntity broadcastMessage =
                broadcastMessageRepository
                        .findByBotSchemeIdAndTamBotIdAndChatChannelIdAndIdAndStateIsNotIn(
                                botScheme.getId(),
                                tamBot.getId().getBotId(),
                                chatChannel.getId().getChatId(),
                                broadcastMessageId,
                                EXCLUDED_STATES
                        );
        if (broadcastMessage == null) {
            // @todo #CC-63 Wrap all exception's messages into string format pattern
            throw new NotFoundEntityException(
                    "Can't find broadcastMessage with id="
                            + broadcastMessageId
                            + " and botSchemeId="
                            + botScheme.getId()
                            + " and tamBotId="
                            + tamBot.getId().getBotId()
                            + " and chatChannelId"
                            + chatChannel.getId().getChatId(),
                    Error.BROADCAST_MESSAGE_DOES_NOT_EXIST
            );
        }
        return broadcastMessage;
    }

    public SuccessResponse getBroadcastMessage(
            final String authToken,
            int botSchemeId,
            long chatChannelId,
            long broadcastMessageId
    ) {
        BotSchemeEntity botScheme = botSchemeService.getBotScheme(authToken, botSchemeId);
        TamBotEntity tamBot = tamBotService.getTamBot(botScheme);
        BroadcastMessageEntity broadcastMessage = getBroadcastMessage(
                botScheme,
                tamBot,
                chatChannelId,
                broadcastMessageId
        );
        return new SuccessResponseWrapper<>(broadcastMessage);
    }

    public SuccessResponse getBroadcastMessages(
            final String authToken,
            int botSchemeId,
            long chatChannelId
    ) {
        BotSchemeEntity botScheme = botSchemeService.getBotScheme(authToken, botSchemeId);
        TamBotEntity tamBot = tamBotService.getTamBot(botScheme);
        ChatChannelEntity chatChannel = chatChannelService.getChatChannel(botScheme, tamBot, chatChannelId);
        List<BroadcastMessageEntity> broadcastMessages =
                broadcastMessageRepository
                        .findAllByBotSchemeIdAndTamBotIdAndChatChannelIdAndStateIsNotIn(
                                botScheme.getId(),
                                tamBot.getId().getBotId(),
                                chatChannel.getId().getChatId(),
                                EXCLUDED_STATES
                        );
        return new SuccessResponseWrapper<>(broadcastMessages);
    }

    public SuccessResponse removeBroadcastMessage(
            final String authToken,
            int botSchemeId,
            long chatChannelId,
            long broadcastMessageId
    ) {
        BotSchemeEntity botScheme = botSchemeService.getBotScheme(authToken, botSchemeId);
        TamBotEntity tamBot = tamBotService.getTamBot(botScheme);
        return new SuccessResponseWrapper<>(
                setBroadcastMessageStateAttempt(
                        () -> getBroadcastMessage(botScheme, tamBot, chatChannelId, broadcastMessageId),
                        message -> BroadcastMessageState.isRemovable(
                                BroadcastMessageState.getById(message.getState())
                        ),
                        BroadcastMessageState.DELETED
                )
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    protected BroadcastMessageEntity setBroadcastMessageStateAttempt(
            Supplier<BroadcastMessageEntity> broadcastMessageSupplier,
            Predicate<BroadcastMessageEntity> broadcastMessagePredicate,
            final BroadcastMessageState targetState
    ) {
        BroadcastMessageEntity broadcastMessage = broadcastMessageSupplier.get();

        if (broadcastMessagePredicate.test(broadcastMessage)
        ) {
            broadcastMessage.setState(targetState);
            broadcastMessageRepository.save(broadcastMessage);
            return broadcastMessage;
        } else {
            throw new UpdateBroadcastMessageException(
                    String.format(
                            "Can't remove broadcast message with id=%d because it is in illegal state=%s",
                            broadcastMessage.getId(),
                            BroadcastMessageState.getById(broadcastMessage.getState()).name()
                    ),
                    Error.BROADCAST_MESSAGE_ILLEGAL_STATE
            );
        }
    }

    public SuccessResponse updateBroadcastMessage(
            final String authToken,
            int botSchemeId,
            long chatChannelId,
            long messageId,
            final BroadcastMessageUpdate broadcastMessageUpdate
    ) {
        BotSchemeEntity botScheme = botSchemeService.getBotScheme(authToken, botSchemeId);
        TamBotEntity tamBotEntity = tamBotService.getTamBot(botScheme);
        BroadcastMessageEntity broadcastMessage =
                getBroadcastMessage(
                        botScheme,
                        tamBotEntity,
                        chatChannelId,
                        messageId
                );
        return new SuccessResponseWrapper<>(updateMessageAttempt(broadcastMessageUpdate, messageId));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    protected BroadcastMessageEntity updateMessageAttempt(
            final BroadcastMessageUpdate broadcastMessageUpdate,
            long messageId
    ) {
        BroadcastMessageEntity broadcastMessage =
                broadcastMessageRepository
                        .findById(messageId)
                        .orElseThrow(
                                () -> new NotFoundEntityException(
                                        String.format(
                                                "Broadcast message with id=%d does not exist",
                                                messageId
                                        ),
                                        Error.BROADCAST_MESSAGE_DOES_NOT_EXIST
                                )
                        );

        if (broadcastMessageUpdate.getTitle() != null) {
            if (!broadcastMessageUpdate.getTitle().isEmpty()) {
                broadcastMessage.setTitle(broadcastMessageUpdate.getTitle());
            } else {
                throw new UpdateBroadcastMessageException(
                        String.format(
                                "Can't update title because it is empty, message id=%d",
                                messageId
                        ),
                        Error.BROADCAST_MESSAGE_TITLE_IS_EMPTY
                );
            }
        }

        switch (BroadcastMessageState.getById(broadcastMessage.getState())) {
            case CREATED:
                createdStateAction.doAction(broadcastMessage, broadcastMessageUpdate);
                break;

            case SCHEDULED:
                scheduledStateAction.doAction(broadcastMessage, broadcastMessageUpdate);
                break;

            case SENT:
                sentStateAction.doAction(broadcastMessage, broadcastMessageUpdate);
                break;

            case DISCARDED_ERASE_BY_USER:
                discardedEraseByUserStateAction.doAction(broadcastMessage, broadcastMessageUpdate);
                break;

            default:
                throw new BroadcastMessageIllegalStateException(
                        "Can't update broadcast message because it is in illegal state",
                        Error.BROADCAST_MESSAGE_ILLEGAL_STATE
                );
        }

        broadcastMessageRepository.save(broadcastMessage);
        return broadcastMessage;
    }

    public SuccessResponse addBroadcastMessage(
            final String authToken,
            int botSchemeId,
            long chatChannelId,
            final BroadcastMessageUpdate broadcastMessageUpdate
    ) {
        if (StringUtils.isEmpty(broadcastMessageUpdate.getTitle())) {
            throw new CreateBroadcastMessageException(
                    "Can't create broadcast message because title is empty",
                    Error.BROADCAST_MESSAGE_TITLE_IS_EMPTY
            );
        }
        BroadcastMessageEntity broadcastMessage = new BroadcastMessageEntity();
        BotSchemeEntity botScheme = botSchemeService.getBotScheme(authToken, botSchemeId);
        TamBotEntity tamBot = tamBotService.getTamBot(botScheme);
        ChatChannelEntity chatChannel = chatChannelService.getChatChannel(botScheme, tamBot, chatChannelId);
        broadcastMessage.setBotSchemeId(botScheme.getId());
        broadcastMessage.setTamBotId(tamBot.getId().getBotId());
        broadcastMessage.setChatChannelId(chatChannel.getId().getChatId());
        broadcastMessage.setTitle(broadcastMessageUpdate.getTitle());
        broadcastMessage.setState(BroadcastMessageState.CREATED);

        return new SuccessResponseWrapper<>(broadcastMessageRepository.save(broadcastMessage));
    }
}
