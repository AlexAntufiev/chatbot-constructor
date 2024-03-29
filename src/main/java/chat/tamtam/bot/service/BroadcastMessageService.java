package chat.tamtam.bot.service;

import chat.tamtam.bot.configuration.logging.Loggable;
import chat.tamtam.bot.domain.bot.BotScheme;
import chat.tamtam.bot.domain.bot.TamBotEntity;
import chat.tamtam.bot.domain.broadcast.message.BroadcastMessage;
import chat.tamtam.bot.domain.broadcast.message.BroadcastMessageState;
import chat.tamtam.bot.domain.broadcast.message.BroadcastMessageUpdate;
import chat.tamtam.bot.domain.broadcast.message.action.CreatedStateAction;
import chat.tamtam.bot.domain.broadcast.message.action.DiscardedEraseByUserStateAction;
import chat.tamtam.bot.domain.broadcast.message.action.ScheduledStateAction;
import chat.tamtam.bot.domain.broadcast.message.action.SentStateAction;
import chat.tamtam.bot.domain.broadcast.message.attachment.BroadcastMessageAttachment;
import chat.tamtam.bot.domain.broadcast.message.attachment.BroadcastMessageAttachmentUpdate;
import chat.tamtam.bot.domain.broadcast.message.attachment.BroadcastMessageWeight;
import chat.tamtam.bot.domain.chatchannel.ChatChannelEntity;
import chat.tamtam.bot.domain.exception.BroadcastMessageIllegalStateException;
import chat.tamtam.bot.domain.exception.CreateBroadcastMessageException;
import chat.tamtam.bot.domain.exception.NotFoundEntityException;
import chat.tamtam.bot.domain.exception.UpdateBroadcastMessageException;
import chat.tamtam.bot.domain.response.SuccessResponse;
import chat.tamtam.bot.domain.response.SuccessResponseWrapper;
import chat.tamtam.bot.repository.BroadcastMessageAttachmentRepository;
import chat.tamtam.bot.repository.BroadcastMessageRepository;
import chat.tamtam.bot.utils.TransactionalUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "tamtam",
        name = {"rss.enabled", "broadcast.enabled"},
        havingValue = "true"
)
public class BroadcastMessageService {
    private static final String ZONED_DATE_TIME_PATTERN = "EEE MMM dd uuuu HH:mm:ss 'GMT'x";
    private final BroadcastMessageRepository broadcastMessageRepository;
    private final BroadcastMessageAttachmentRepository broadcastMessageAttachmentRepository;
    private final BotSchemeService botSchemeService;
    private final TamBotService tamBotService;
    private final ChatChannelService chatChannelService;
    private static final List<Byte> EXCLUDED_STATES =
            Collections.singletonList(BroadcastMessageState.DELETED.getValue());

    private final TransactionalUtils transactionalUtils;

    // Broadcast message actions
    private final CreatedStateAction createdStateAction;
    private final DiscardedEraseByUserStateAction discardedEraseByUserStateAction;
    private final ScheduledStateAction scheduledStateAction;
    private final SentStateAction sentStateAction;

    @Loggable
    public BroadcastMessage getBroadcastMessage(
            final BotScheme botScheme,
            final TamBotEntity tamBot,
            final Long chatChannelId,
            final Long broadcastMessageId
    ) {
        ChatChannelEntity chatChannel = chatChannelService.getChatChannel(botScheme, tamBot, chatChannelId);
        BroadcastMessage broadcastMessage =
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

    @Loggable
    public SuccessResponse getBroadcastMessage(
            final String authToken,
            int botSchemeId,
            long chatChannelId,
            long broadcastMessageId
    ) {
        BotScheme botScheme = botSchemeService.getBotScheme(authToken, botSchemeId);
        TamBotEntity tamBot = tamBotService.getTamBot(botScheme);
        BroadcastMessage broadcastMessage = getBroadcastMessage(
                botScheme,
                tamBot,
                chatChannelId,
                broadcastMessageId
        );
        return new SuccessResponseWrapper<>(broadcastMessage);
    }

    @Loggable
    public SuccessResponse getBroadcastMessages(
            final String authToken,
            int botSchemeId,
            long chatChannelId
    ) {
        BotScheme botScheme = botSchemeService.getBotScheme(authToken, botSchemeId);
        TamBotEntity tamBot = tamBotService.getTamBot(botScheme);
        ChatChannelEntity chatChannel = chatChannelService.getChatChannel(botScheme, tamBot, chatChannelId);
        List<BroadcastMessage> broadcastMessages =
                broadcastMessageRepository
                        .findAllByBotSchemeIdAndTamBotIdAndChatChannelIdAndStateIsNotIn(
                                botScheme.getId(),
                                tamBot.getId().getBotId(),
                                chatChannel.getId().getChatId(),
                                EXCLUDED_STATES
                        );
        return new SuccessResponseWrapper<>(broadcastMessages);
    }

    @Loggable
    public SuccessResponse removeBroadcastMessage(
            final String authToken,
            int botSchemeId,
            long chatChannelId,
            long broadcastMessageId
    ) {
        BotScheme botScheme = botSchemeService.getBotScheme(authToken, botSchemeId);
        TamBotEntity tamBot = tamBotService.getTamBot(botScheme);
        return new SuccessResponseWrapper<>(
                transactionalUtils.invokeCallable(
                        () -> setBroadcastMessageStateAttempt(
                                () -> getBroadcastMessage(botScheme, tamBot, chatChannelId, broadcastMessageId),
                                message -> BroadcastMessageState.isRemovable(
                                        BroadcastMessageState.getById(message.getState())
                                ),
                                BroadcastMessageState.DELETED
                        )
                )
        );
    }

    private BroadcastMessage setBroadcastMessageStateAttempt(
            Supplier<BroadcastMessage> broadcastMessageSupplier,
            Predicate<BroadcastMessage> broadcastMessagePredicate,
            final BroadcastMessageState targetState
    ) {
        BroadcastMessage broadcastMessage = broadcastMessageSupplier.get();

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

    @Loggable
    public SuccessResponse updateBroadcastMessage(
            final String authToken,
            int botSchemeId,
            long chatChannelId,
            long messageId,
            final BroadcastMessageUpdate broadcastMessageUpdate
    ) {
        BotScheme botScheme = botSchemeService.getBotScheme(authToken, botSchemeId);
        TamBotEntity tamBotEntity = tamBotService.getTamBot(botScheme);
        BroadcastMessage broadcastMessage =
                getBroadcastMessage(
                        botScheme,
                        tamBotEntity,
                        chatChannelId,
                        messageId
                );
        return new SuccessResponseWrapper<>(
                transactionalUtils.invokeCallable(
                        () -> updateMessageAttempt(broadcastMessageUpdate, messageId)
                )
        );
    }

    private BroadcastMessage updateMessageAttempt(
            final BroadcastMessageUpdate broadcastMessageUpdate,
            long messageId
    ) {
        BroadcastMessage broadcastMessage =
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

    @Loggable
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
        BroadcastMessage broadcastMessage = new BroadcastMessage();
        BotScheme botScheme = botSchemeService.getBotScheme(authToken, botSchemeId);
        TamBotEntity tamBot = tamBotService.getTamBot(botScheme);
        ChatChannelEntity chatChannel = chatChannelService.getChatChannel(botScheme, tamBot, chatChannelId);
        broadcastMessage.setBotSchemeId(botScheme.getId());
        broadcastMessage.setTamBotId(tamBot.getId().getBotId());
        broadcastMessage.setChatChannelId(chatChannel.getId().getChatId());
        broadcastMessage.setTitle(broadcastMessageUpdate.getTitle());
        broadcastMessage.setState(BroadcastMessageState.CREATED);

        return new SuccessResponseWrapper<>(broadcastMessageRepository.save(broadcastMessage));
    }

   public SuccessResponse addBroadcastMessageAttachment(
            final String authToken,
            final int botSchemeId,
            final long chatChannelId,
            final long broadcastMessageId,
            final BroadcastMessageAttachmentUpdate broadcastMessageAttachmentUpdate
    ) {
        BotScheme botScheme = botSchemeService.getBotScheme(authToken, botSchemeId);
        TamBotEntity tamBot = tamBotService.getTamBot(botScheme);
        return (SuccessResponse) transactionalUtils.invokeCallable(() -> {
            BroadcastMessage broadcastMessage =
                    getBroadcastMessage(botScheme, tamBot, chatChannelId, broadcastMessageId);
            BroadcastMessageState state = BroadcastMessageState.getById(broadcastMessage.getState());
            if (!BroadcastMessageState.isAttachmentUpdatable(state)) {
                throw new UpdateBroadcastMessageException(
                        String.format(
                                "Can't update broadcast message(id=%d) attachments because it is in state=%s",
                                broadcastMessageId,
                                state.name()
                        ),
                        Error.BROADCAST_MESSAGE_ILLEGAL_STATE
                );
            }
            BroadcastMessageAttachment attachment = new BroadcastMessageAttachment(
                    broadcastMessageAttachmentUpdate.getType(),
                    broadcastMessageAttachmentUpdate.getToken(),
                    broadcastMessage.getId(),
                    broadcastMessageAttachmentUpdate.getTitle()
            );

            byte currentWeight = (byte) (broadcastMessage.getWeight()
                    + BroadcastMessageWeight.getWeight(attachment));

            if (BroadcastMessageWeight.isWeightExceedsMax(currentWeight)) {
                throw new UpdateBroadcastMessageException(
                        String.format(
                                "Can't add attachment with type=%s to broadcast message with id=%d "
                                        + "because it has enough attachments already(message weight=%d)",
                                attachment.getUploadType(),
                                broadcastMessage.getId(),
                                broadcastMessage.getWeight()
                        ),
                        Error.BROADCAST_MESSAGE_HAS_TOO_MUCH_ATTACHMENTS
                );
            }

            broadcastMessage.setWeight((byte) currentWeight);

            broadcastMessageRepository.save(broadcastMessage);

            return new SuccessResponseWrapper<>(
                    broadcastMessageAttachmentRepository.save(attachment)
            );
        });
    }

    public SuccessResponse removeBroadcastMessageAttachment(
            final String authToken,
            final int botSchemeId,
            final long chatChannelId,
            final long broadcastMessageId,
            final long attachmentId
    ) {
        BotScheme botScheme = botSchemeService.getBotScheme(authToken, botSchemeId);
        TamBotEntity tamBot = tamBotService.getTamBot(botScheme);
        transactionalUtils.invokeRunnable(() -> {
            BroadcastMessage broadcastMessage =
                    getBroadcastMessage(botScheme, tamBot, chatChannelId, broadcastMessageId);
            BroadcastMessageState state = BroadcastMessageState.getById(broadcastMessage.getState());

            if (!BroadcastMessageState.isAttachmentUpdatable(state)) {
                throw new UpdateBroadcastMessageException(
                        String.format(
                                "Can't remove attachment with id=%d "
                                        + "because broadcast message with id=%d is in state=%s",
                                attachmentId,
                                broadcastMessageId,
                                state.name()
                        ),
                        Error.BROADCAST_MESSAGE_ILLEGAL_STATE
                );
            }

            BroadcastMessageAttachment attachment = getAttachment(attachmentId, broadcastMessage.getId());
            attachment.setBroadcastMessageId(null);

            broadcastMessage.setWeight(
                    (byte) (broadcastMessage.getWeight() - BroadcastMessageWeight.getWeight(attachment))
            );

            broadcastMessageRepository.save(broadcastMessage);
            broadcastMessageAttachmentRepository.save(attachment);
        });
        return new SuccessResponse();
    }

    private BroadcastMessageAttachment getAttachment(
            final long attachmentId,
            final long messageId
    ) {
        return broadcastMessageAttachmentRepository
                .findById(attachmentId)
                .filter(o -> {
                    if (o.getBroadcastMessageId() == null) {
                        return false;
                    }
                    return o.getBroadcastMessageId() == messageId;
                })
                .orElseThrow(() -> new NotFoundEntityException(
                        String.format(
                                "Can't find broadcast message attachment with id=%d and messageId=%d",
                                attachmentId,
                                messageId
                        ),
                        Error.ATTACHMENT_DOES_NOT_EXIST
                ));
    }

    public SuccessResponse getBroadcastMessageAttachments(
            final String authToken,
            final int botSchemeId,
            final long chatChannelId,
            final long broadcastMessageId
    ) {
        BotScheme botScheme = botSchemeService.getBotScheme(authToken, botSchemeId);
        TamBotEntity tamBot = tamBotService.getTamBot(botScheme);
        BroadcastMessage broadcastMessage =
                getBroadcastMessage(botScheme, tamBot, chatChannelId, broadcastMessageId);
        return new SuccessResponseWrapper<>(
                broadcastMessageAttachmentRepository.findAllByBroadcastMessageId(broadcastMessageId)
        );
    }
}
