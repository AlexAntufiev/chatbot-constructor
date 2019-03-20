package chat.tamtam.bot.service;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import chat.tamtam.bot.domain.bot.BotSchemeEntity;
import chat.tamtam.bot.domain.bot.TamBotEntity;
import chat.tamtam.bot.domain.broadcast.message.BroadcastMessageEntity;
import chat.tamtam.bot.domain.broadcast.message.BroadcastMessageState;
import chat.tamtam.bot.domain.broadcast.message.NewBroadcastMessage;
import chat.tamtam.bot.domain.chatchannel.ChatChannelEntity;
import chat.tamtam.bot.domain.exception.CreateBroadcastMessageException;
import chat.tamtam.bot.domain.exception.NotFoundEntityException;
import chat.tamtam.bot.domain.response.SuccessResponse;
import chat.tamtam.bot.domain.response.SuccessResponseWrapper;
import chat.tamtam.bot.repository.BroadcastMessageRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BroadcastMessageService {
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
        broadcastMessage.setState(BroadcastMessageState.DELETED);
        return new SuccessResponseWrapper<>(broadcastMessage);
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

    private BroadcastMessageEntity transformToBroadcastMessageEntity(final NewBroadcastMessage newBroadcastMessage) {
        // @todo #CC-63 Expand broadcastMessage filtering(payload check etc.)
        if (StringUtils.isEmpty(newBroadcastMessage.getTitle())) {
            throw new CreateBroadcastMessageException(
                    "Can't create broadCastMessage because name is empty",
                    Error.BROADCAST_MESSAGE_TITLE_IS_EMPTY
            );
        }
        if (newBroadcastMessage.getFiringTime() == null) {
            throw new CreateBroadcastMessageException(
                    "Can't create broadCastMessage because firing time is null",
                    Error.BROADCAST_MESSAGE_FIRING_TIME_IS_NULL
            );
        }
        Timestamp localTimeStamp = new Timestamp(System.currentTimeMillis());
        if (!localTimeStamp.after(newBroadcastMessage.getFiringTime())) {
            throw new CreateBroadcastMessageException(
                    "Can't create broadCastMessage because firing time in past="
                            + newBroadcastMessage.getFiringTime()
                            + " and local time="
                            + localTimeStamp,
                    Error.BROADCAST_MESSAGE_FIRING_TIME_IS_IN_PAST
            );
        }
        BroadcastMessageEntity broadcastMessage = new BroadcastMessageEntity();
        broadcastMessage.setTitle(newBroadcastMessage.getTitle());
        broadcastMessage.setFiringTime(newBroadcastMessage.getFiringTime());
        broadcastMessage.setText(newBroadcastMessage.getText());
        return broadcastMessage;
    }
}
