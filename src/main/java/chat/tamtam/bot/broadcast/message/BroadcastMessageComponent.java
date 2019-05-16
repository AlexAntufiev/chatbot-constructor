package chat.tamtam.bot.broadcast.message;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import org.jetbrains.annotations.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import chat.tamtam.bot.domain.bot.BotScheme;
import chat.tamtam.bot.domain.bot.TamBotEntity;
import chat.tamtam.bot.domain.broadcast.message.BroadcastMessage;
import chat.tamtam.bot.domain.broadcast.message.BroadcastMessageState;
import chat.tamtam.bot.domain.notification.NotificationMessage;
import chat.tamtam.bot.domain.notification.NotificationType;
import chat.tamtam.bot.repository.BotSchemeRepository;
import chat.tamtam.bot.repository.BroadcastMessageAttachmentRepository;
import chat.tamtam.bot.repository.BroadcastMessageRepository;
import chat.tamtam.bot.repository.TamBotRepository;
import chat.tamtam.bot.service.Error;
import chat.tamtam.bot.service.notification.NotificationService;
import chat.tamtam.bot.utils.TransactionalUtils;
import chat.tamtam.botapi.TamTamBotAPI;
import chat.tamtam.botapi.exceptions.APIException;
import chat.tamtam.botapi.exceptions.ClientException;
import chat.tamtam.botapi.model.AttachmentRequest;
import chat.tamtam.botapi.model.AudioAttachmentRequest;
import chat.tamtam.botapi.model.FileAttachmentRequest;
import chat.tamtam.botapi.model.NewMessageBody;
import chat.tamtam.botapi.model.PhotoAttachmentRequest;
import chat.tamtam.botapi.model.PhotoAttachmentRequestPayload;
import chat.tamtam.botapi.model.SendMessageResult;
import chat.tamtam.botapi.model.UploadType;
import chat.tamtam.botapi.model.UploadedFileInfo;
import chat.tamtam.botapi.model.UploadedInfo;
import chat.tamtam.botapi.model.VideoAttachmentRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
@ConditionalOnProperty(
        prefix = "tamtam.broadcast",
        name = "enabled",
        havingValue = "true"
)
@RequiredArgsConstructor
@RefreshScope
public class BroadcastMessageComponent {
    private static final long DEFAULT_SENDING_RATE = 10_000L;
    private static final long DEFAULT_ERASING_RATE = 10_000L;

    private final BroadcastMessageRepository broadcastMessageRepository;
    private final BotSchemeRepository botSchemeRepository;
    private final TamBotRepository tamBotRepository;
    private final BroadcastMessageAttachmentRepository broadcastMessageAttachmentRepository;
    private final NotificationService notificationService;

    private final TransactionalUtils transactionalUtils;

    private final Executor scheduledMessagesExecutor;

    @Scheduled(fixedRate = DEFAULT_SENDING_RATE)
    public void fireScheduledMessages() {
        Instant currentInstant = Instant.now();
        List<BroadcastMessage> scheduledMessages =
                broadcastMessageRepository.findAllByFiringTimeBeforeAndState(
                        currentInstant,
                        BroadcastMessageState.SCHEDULED.getValue()
                );
        scheduledMessages.forEach(message -> {
            botSchemeRepository
                    .findById(message.getBotSchemeId())
                    .ifPresentOrElse(
                            botScheme -> {
                                TamTamBotAPI tamTamBotAPI = getTamTamBotAPI(message, botScheme);
                                if (tamTamBotAPI != null) {
                                    try {
                                        transactionalUtils.invokeRunnable(
                                                () -> setProcessingStateAttempt(
                                                        message,
                                                        BroadcastMessageState.SCHEDULED,
                                                        botScheme.getUserId()
                                                )
                                        );
                                        scheduledMessagesExecutor
                                                .execute(() -> sendBroadcastMessage(
                                                        tamTamBotAPI,
                                                        message,
                                                        botScheme.getUserId()
                                                ));
                                    } catch (IllegalStateException iSE) {
                                        log.error(String.format(
                                                "Can't change message state with id=%d",
                                                message.getId()
                                        ), iSE);
                                    }
                                } else {
                                    message.setState(BroadcastMessageState.ERROR);
                                    message.setError(Error.BROADCAST_MESSAGE_ILLEGAL_STATE.getErrorKey());
                                    broadcastMessageRepository.save(message);
                                }
                            },
                            () -> log.error(
                                    String.format(
                                            "Can't send message with id=%d because botScheme is not presented",
                                            message.getBotSchemeId()
                                    )
                            )
                    );

        });
    }

    protected void setProcessingStateAttempt(
            final BroadcastMessage broadcastMessage,
            final BroadcastMessageState requiredState,
            final long userId
    ) throws IllegalStateException {
        if (broadcastMessageRepository
                .findById(broadcastMessage.getId())
                .get()
                .getState() == requiredState.getValue()) {
            broadcastMessage.setState(BroadcastMessageState.PROCESSING);
            broadcastMessageRepository.save(broadcastMessage);
            notifyUser(userId, broadcastMessage);
        } else {
            throw new IllegalStateException(
                    "Can't set PROCESSING state because message is not in "
                            + requiredState.name()
                            + " state");
        }
    }

    @Scheduled(fixedRate = DEFAULT_ERASING_RATE)
    public void eraseScheduledMessages() {
        Instant currentInstant = Instant.now();
        List<BroadcastMessage> scheduledMessages =
                broadcastMessageRepository.findAllByErasingTimeBeforeAndState(
                        currentInstant,
                        BroadcastMessageState.SENT.getValue()
                );
        scheduledMessages.forEach(message -> {
            botSchemeRepository
                    .findById(message.getBotSchemeId())
                    .ifPresentOrElse(
                            botScheme -> {
                                TamTamBotAPI tamTamBotAPI = getTamTamBotAPI(message, botScheme);
                                if (tamTamBotAPI != null) {
                                    try {
                                        transactionalUtils.invokeRunnable(
                                                () -> setProcessingStateAttempt(
                                                        message,
                                                        BroadcastMessageState.SENT,
                                                        botScheme.getUserId()
                                                )
                                        );
                                        scheduledMessagesExecutor
                                                .execute(() -> eraseBroadcastMessage(
                                                        tamTamBotAPI,
                                                        message,
                                                        botScheme.getUserId()
                                                ));
                                    } catch (IllegalStateException iSE) {
                                        log.error(String.format(
                                                "Can't change message state with id=%d",
                                                message.getId()
                                        ), iSE);
                                    }
                                } else {
                                    message.setState(BroadcastMessageState.ERROR);
                                    message.setError(Error.BROADCAST_MESSAGE_ILLEGAL_STATE.getErrorKey());
                                    broadcastMessageRepository.save(message);
                                }
                            },
                            () -> log.error(
                                    String.format(
                                            "Can't erase message with id=%d because botScheme is not presented",
                                            message.getBotSchemeId()
                                    )
                            )
                    );

        });
    }

    @Async
    protected void sendBroadcastMessage(
            final TamTamBotAPI tamTamBotAPI,
            final BroadcastMessage broadcastMessage,
            final long userId
    ) {
        try {
            List<AttachmentRequest> attachmentRequests = new ArrayList<>();

            broadcastMessageAttachmentRepository
                    .findAllByBroadcastMessageId(broadcastMessage.getId())
                    .iterator()
                    .forEachRemaining(attachment -> {
                        AttachmentRequest attachmentRequest = null;

                        UploadType uploadType = attachment.getUploadType();

                        switch (uploadType) {
                            case PHOTO:
                                attachmentRequest =
                                        new PhotoAttachmentRequest(
                                                new PhotoAttachmentRequestPayload().token(
                                                        new String(attachment.getAttachmentIdentifier())
                                                )
                                        );
                                break;
                            case FILE:
                                attachmentRequest =
                                        new FileAttachmentRequest(
                                                new UploadedFileInfo(
                                                        ByteBuffer.wrap(attachment.getAttachmentIdentifier()).getLong()
                                                )
                                        );
                                break;
                            case AUDIO:
                                attachmentRequest =
                                        new AudioAttachmentRequest(
                                                new UploadedInfo(
                                                        ByteBuffer.wrap(attachment.getAttachmentIdentifier()).getLong()
                                                )
                                        );
                                break;
                            case VIDEO:
                                attachmentRequest =
                                        new VideoAttachmentRequest(
                                                new UploadedInfo(
                                                        ByteBuffer.wrap(attachment.getAttachmentIdentifier()).getLong()
                                                )
                                        );
                                break;
                            default:
                                throw new IllegalStateException(
                                        String.format(
                                                "Illegal type %s of attachment with id=%d",
                                                uploadType,
                                                attachment.getId()
                                        )
                                );
                        }

                        attachmentRequests.add(attachmentRequest);
            });

            SendMessageResult sendMessageResult =
                    tamTamBotAPI
                            .sendMessage(new NewMessageBody(broadcastMessage.getText(), attachmentRequests))
                            .chatId(broadcastMessage.getChatChannelId())
                            .execute();
            broadcastMessage.setMessageId(sendMessageResult.getMessage().getBody().getMid());
            broadcastMessage.setState(BroadcastMessageState.SENT);
        } catch (APIException | ClientException | IllegalStateException ex) {
            log.error(String.format("Can't send scheduled message with id=%d", broadcastMessage.getId()), ex);
            broadcastMessage.setState(BroadcastMessageState.ERROR);
            broadcastMessage.setError(Error.BROADCAST_MESSAGE_SEND_ERROR.getErrorKey());
        }
        broadcastMessageRepository.save(broadcastMessage);
        notifyUser(userId, broadcastMessage);
    }

    @Async
    protected void eraseBroadcastMessage(
            final TamTamBotAPI tamTamBotAPI,
            final BroadcastMessage broadcastMessage,
            final long userId
    ) {
        try {
            tamTamBotAPI
                    .deleteMessage(broadcastMessage.getMessageId())
                    .execute();
            broadcastMessage.setState(BroadcastMessageState.ERASED_BY_SCHEDULE);
        } catch (APIException | ClientException ex) {
            log.error(String.format("Can't erase scheduled message with id=%d", broadcastMessage.getId()), ex);
            broadcastMessage.setState(BroadcastMessageState.ERROR);
            broadcastMessage.setError(Error.BROADCAST_MESSAGE_ERASE_ERROR.getErrorKey());
        }
        broadcastMessageRepository.save(broadcastMessage);
        notifyUser(userId, broadcastMessage);
    }

    private @Nullable TamTamBotAPI getTamTamBotAPI(BroadcastMessage e, BotScheme botScheme) {
        TamBotEntity tamBot =
                tamBotRepository.findById(new TamBotEntity.Id(botScheme.getBotId(), botScheme.getUserId()));
        if (tamBot == null) {
            log.error(
                    String.format(
                            "Can't process scheduled message with id=%d because related tamBot not found",
                            e.getId()
                    ),
                    e
            );
            return null;
        }
        return TamTamBotAPI.create(tamBot.getToken());
    }

    private void notifyUser(final long userId, final BroadcastMessage broadcastMessage) {
        notificationService.notifyUser(
                userId,
                new NotificationMessage(
                        NotificationType.BROADCAST_MESSAGE_NOTIFICATION,
                        broadcastMessage
                )
        );
    }
}
