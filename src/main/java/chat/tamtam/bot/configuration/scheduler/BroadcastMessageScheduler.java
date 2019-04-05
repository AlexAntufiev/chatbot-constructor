package chat.tamtam.bot.configuration.scheduler;

import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import org.jetbrains.annotations.Nullable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import chat.tamtam.bot.domain.bot.BotSchemeEntity;
import chat.tamtam.bot.domain.bot.TamBotEntity;
import chat.tamtam.bot.domain.broadcast.message.BroadcastMessageEntity;
import chat.tamtam.bot.domain.broadcast.message.BroadcastMessageState;
import chat.tamtam.bot.domain.notification.NotificationMessage;
import chat.tamtam.bot.domain.notification.NotificationType;
import chat.tamtam.bot.repository.BotSchemaRepository;
import chat.tamtam.bot.repository.BroadcastMessageAttachmentRepository;
import chat.tamtam.bot.repository.BroadcastMessageRepository;
import chat.tamtam.bot.repository.TamBotRepository;
import chat.tamtam.bot.service.Error;
import chat.tamtam.bot.service.TransactionalUtils;
import chat.tamtam.bot.service.notification.NotificationService;
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
@RequiredArgsConstructor
public class BroadcastMessageScheduler {
    private static final long DEFAULT_SENDING_RATE = 10_000L;
    private static final long DEFAULT_ERASING_RATE = 10_000L;

    private final BroadcastMessageRepository broadcastMessageRepository;
    private final BotSchemaRepository botSchemaRepository;
    private final TamBotRepository tamBotRepository;
    private final BroadcastMessageAttachmentRepository broadcastMessageAttachmentRepository;
    private final NotificationService notificationService;

    private final TransactionalUtils transactionalUtils;

    private final Executor scheduledMessagesExecutor;

    @Scheduled(fixedRate = DEFAULT_SENDING_RATE)
    public void fireScheduledMessages() {
        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
        List<BroadcastMessageEntity> scheduledMessages =
                broadcastMessageRepository.findAllByFiringTimeBeforeAndState(
                        currentTimestamp,
                        BroadcastMessageState.SCHEDULED.getValue()
                );
        scheduledMessages.forEach(e -> {
            botSchemaRepository
                    .findById(e.getBotSchemeId())
                    .ifPresentOrElse(
                            botScheme -> {
                                TamTamBotAPI tamTamBotAPI = getTamTamBotAPI(e, botScheme);
                                if (tamTamBotAPI != null) {
                                    try {
                                        transactionalUtils.invokeRunnable(
                                                () -> setProcessingStateAttempt(
                                                        e,
                                                        BroadcastMessageState.SCHEDULED,
                                                        botScheme.getUserId()
                                                )
                                        );
                                        scheduledMessagesExecutor
                                                .execute(() -> sendBroadcastMessage(
                                                        tamTamBotAPI,
                                                        e,
                                                        botScheme.getUserId()
                                                ));
                                    } catch (IllegalStateException iSE) {
                                        log.error(String.format(
                                                "Can't change message state with id=%d",
                                                e.getId()
                                        ), iSE);
                                    }
                                } else {
                                    e.setState(BroadcastMessageState.ERROR);
                                    e.setError(Error.BROADCAST_MESSAGE_ILLEGAL_STATE.getErrorKey());
                                    broadcastMessageRepository.save(e);
                                }
                            },
                            () -> log.error(
                                    String.format(
                                            "Can't send message with id=%d because botScheme is not presented",
                                            e.getBotSchemeId()
                                    )
                            )
                    );

        });
    }

    protected void setProcessingStateAttempt(
            final BroadcastMessageEntity broadcastMessage,
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
        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
        List<BroadcastMessageEntity> scheduledMessages =
                broadcastMessageRepository.findAllByErasingTimeBeforeAndState(
                        currentTimestamp,
                        BroadcastMessageState.SENT.getValue()
                );
        scheduledMessages.forEach(e -> {
            botSchemaRepository
                    .findById(e.getBotSchemeId())
                    .ifPresentOrElse(
                            botScheme -> {
                                TamTamBotAPI tamTamBotAPI = getTamTamBotAPI(e, botScheme);
                                if (tamTamBotAPI != null) {
                                    try {
                                        transactionalUtils.invokeRunnable(
                                                () -> setProcessingStateAttempt(
                                                        e,
                                                        BroadcastMessageState.SENT,
                                                        botScheme.getUserId()
                                                )
                                        );
                                        scheduledMessagesExecutor
                                                .execute(() -> eraseBroadcastMessage(
                                                        tamTamBotAPI,
                                                        e,
                                                        botScheme.getUserId()
                                                ));
                                    } catch (IllegalStateException iSE) {
                                        log.error(String.format(
                                                "Can't change message state with id=%d",
                                                e.getId()
                                        ), iSE);
                                    }
                                } else {
                                    e.setState(BroadcastMessageState.ERROR);
                                    e.setError(Error.BROADCAST_MESSAGE_ILLEGAL_STATE.getErrorKey());
                                    broadcastMessageRepository.save(e);
                                }
                            },
                            () -> log.error(
                                    String.format(
                                            "Can't erase message with id=%d because botScheme is not presented",
                                            e.getBotSchemeId()
                                    )
                            )
                    );

        });
    }

    @Async
    protected void sendBroadcastMessage(
            final TamTamBotAPI tamTamBotAPI,
            final BroadcastMessageEntity broadcastMessage,
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
            final BroadcastMessageEntity broadcastMessage,
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

    private @Nullable TamTamBotAPI getTamTamBotAPI(BroadcastMessageEntity e, BotSchemeEntity botScheme) {
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

    private void notifyUser(final long userId, final BroadcastMessageEntity broadcastMessage) {
        notificationService.notifyUser(
                userId,
                new NotificationMessage(
                        NotificationType.BROADCAST_MESSAGE_NOTIFICATION,
                        broadcastMessage
                )
        );
    }
}
