package chat.tamtam.bot.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Supplier;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import chat.tamtam.bot.domain.bot.BotSchemeEntity;
import chat.tamtam.bot.domain.bot.TamBotEntity;
import chat.tamtam.bot.domain.broadcast.message.BroadcastMessageEntity;
import chat.tamtam.bot.domain.broadcast.message.BroadcastMessageState;
import chat.tamtam.bot.domain.broadcast.message.NewBroadcastMessage;
import chat.tamtam.bot.domain.chatchannel.ChatChannelEntity;
import chat.tamtam.bot.domain.exception.BroadcastMessageIllegalStateException;
import chat.tamtam.bot.domain.exception.ChatBotConstructorException;
import chat.tamtam.bot.domain.exception.CreateBroadcastMessageException;
import chat.tamtam.bot.domain.exception.NotFoundEntityException;
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

    public BroadcastMessageEntity getBroadcastMessage(
            final BotSchemeEntity botScheme,
            final TamBotEntity tamBot,
            final Long chatChannelId,
            final Long broadcastMessageId
    ) {
        ChatChannelEntity chatChannel = chatChannelService.getChatChannel(botScheme, tamBot, chatChannelId);
        BroadcastMessageEntity broadcastMessage =
                broadcastMessageRepository
                        .findByBotSchemeIdAndTamBotIdAndChatChannelIdAndId(
                                botScheme.getId(),
                                tamBot.getId().getBotId(),
                                chatChannel.getId().getChatId(),
                                broadcastMessageId
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
                        .findAllByBotSchemeIdAndTamBotIdAndChatChannelId(
                                botScheme.getId(),
                                tamBot.getId().getBotId(),
                                chatChannel.getId().getChatId()
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
        BroadcastMessageEntity broadcastMessage = getBroadcastMessage(
                botScheme,
                tamBot,
                chatChannelId,
                broadcastMessageId
        );
        try {
            setBroadcastMessageStateAttempt(
                    broadcastMessage,
                    BroadcastMessageState.SCHEDULED,
                    BroadcastMessageState.DELETED
            );
            return new SuccessResponseWrapper<>(broadcastMessage);
        } catch (IllegalStateException iSE) {
            throw new BroadcastMessageIllegalStateException(iSE, Error.BROADCAST_MESSAGE_ILLEGAL_STATE.getErrorKey());
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    protected void setBroadcastMessageStateAttempt(
            final BroadcastMessageEntity broadcastMessage,
            final BroadcastMessageState requiredState,
            final BroadcastMessageState targetState
    ) {
        if (broadcastMessageRepository
                .findById(broadcastMessage.getId())
                .get()
                .getState() == requiredState.getValue()
        ) {
            broadcastMessage.setState(targetState);
            broadcastMessageRepository.save(broadcastMessage);
        } else {
            throw new IllegalStateException(
                    "Can't set "
                            + targetState.name()
                            + " state because message is not in "
                            + requiredState.name()
                            + " state");
        }
    }

    public SuccessResponse addBroadcastMessage(
            final String authToken,
            int botSchemeId,
            long chatChannelId,
            final NewBroadcastMessage newBroadcastMessage
    ) {
        BroadcastMessageEntity broadcastMessage = transformToBroadcastMessageEntity(newBroadcastMessage);
        BotSchemeEntity botScheme = botSchemeService.getBotScheme(authToken, botSchemeId);
        TamBotEntity tamBot = tamBotService.getTamBot(botScheme);
        ChatChannelEntity chatChannel = chatChannelService.getChatChannel(botScheme, tamBot, chatChannelId);
        broadcastMessage.setBotSchemeId(botScheme.getId());
        broadcastMessage.setTamBotId(tamBot.getId().getBotId());
        broadcastMessage.setChatChannelId(chatChannel.getId().getChatId());
        broadcastMessage.setState(BroadcastMessageState.SCHEDULED);
        return new SuccessResponseWrapper<>(broadcastMessageRepository.save(broadcastMessage));
    }

    private static BroadcastMessageEntity transformToBroadcastMessageEntity(
            final NewBroadcastMessage newBroadcastMessage
    ) {
        // @todo #CC-63 Expand broadcastMessage filtering(payload check etc.)
        if (StringUtils.isEmpty(newBroadcastMessage.getTitle())) {
            throw new CreateBroadcastMessageException(
                    "Can't create broadCastMessage because title is empty",
                    Error.BROADCAST_MESSAGE_TITLE_IS_EMPTY
            );
        }
        if (newBroadcastMessage.getFiringTime() == null) {
            throw new ChatBotConstructorException(
                    "Can't create broadCastMessage because firing time is null",
                    Error.BROADCAST_MESSAGE_FIRING_TIME_IS_NULL
            );
        }
        Timestamp localTimestamp = Timestamp.valueOf(LocalDateTime.now());
        Timestamp firingTimestamp = getTimestamp(
                newBroadcastMessage::getFiringTime,
                localTimestamp,
                "Can't create broadCastMessage because firing time=%d is in past and local time=%d",
                Error.BROADCAST_MESSAGE_FIRING_TIME_IS_IN_PAST
        );
        Timestamp erasingTimestamp = null;
        if (newBroadcastMessage.getErasingTime() != null) {
            erasingTimestamp =
                    getTimestamp(
                            newBroadcastMessage::getErasingTime,
                            firingTimestamp,
                            "Can't create broadCastMessage because erasing time=%d is before then firing time=%d",
                            Error.BROADCAST_MESSAGE_ERASING_TIME_IS_BEFORE_THEN_FIRING_TIME
                    );
        }
        BroadcastMessageEntity broadcastMessage = new BroadcastMessageEntity();
        broadcastMessage.setTitle(newBroadcastMessage.getTitle());
        broadcastMessage.setFiringTime(firingTimestamp);
        broadcastMessage.setErasingTime(erasingTimestamp);
        broadcastMessage.setText(newBroadcastMessage.getText());
        return broadcastMessage;
    }

    private static Timestamp getTimestamp(
            final Supplier<Long> supplier,
            final Timestamp baseTime,
            final String baseTimeAfterSuppliedTimeMessageFormat,
            final Error baseTimeAfterSuppliedTimeError
    ) {
        // @todo #CC-63 Add value to props that will be minimal diff between base-time and supplied-time
        // @todo #CC-63 Fix timestamp representation, now it builds time-date string using wrong timezone
        Timestamp futureActionTimestamp = new Timestamp(baseTime.getTime() + supplier.get());
        if (!futureActionTimestamp.after(baseTime)) {
            throw new CreateBroadcastMessageException(
                    String.format(
                            baseTimeAfterSuppliedTimeMessageFormat,
                            futureActionTimestamp.getTime(),
                            baseTime.getTime()
                    ),
                    baseTimeAfterSuppliedTimeError
            );
        }
        return futureActionTimestamp;
    }
}
