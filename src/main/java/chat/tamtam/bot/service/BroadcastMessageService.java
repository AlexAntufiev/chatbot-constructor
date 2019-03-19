package chat.tamtam.bot.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import chat.tamtam.bot.domain.bot.BotSchemeEntity;
import chat.tamtam.bot.domain.bot.TamBotEntity;
import chat.tamtam.bot.domain.broadcast.message.BroadcastMessageEntity;
import chat.tamtam.bot.domain.broadcast.message.BroadcastMessageState;
import chat.tamtam.bot.domain.chatchannel.ChatChannelEntity;
import chat.tamtam.bot.domain.exception.CreateBroadcastMessageException;
import chat.tamtam.bot.domain.exception.NotFoundEntityException;
import chat.tamtam.bot.domain.response.BroadcastMessageListSuccessReponse;
import chat.tamtam.bot.domain.response.BroadcastMessageSuccessResponse;
import chat.tamtam.bot.domain.response.SuccessResponse;
import chat.tamtam.bot.repository.BroadcastMessageRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BroadcastMessageService {
    private final BroadcastMessageRepository broadcastMessageRepository;
    private final BotSchemeService botSchemeService;
    private final TamBotService tamBotService;
    private final ChatChannelService chatChannelService;

    public SuccessResponse getBroadcastMessage(
            final String authToken,
            int botSchemeId,
            long chatChannelId,
            long broadcastMessageId
    ) {
        BotSchemeEntity botScheme = botSchemeService.getBotScheme(authToken, botSchemeId);
        TamBotEntity tamBot = tamBotService.getTamBot(botScheme);
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
            throw new NotFoundEntityException(
                    "Can't find broadcastMessage with id="
                            + broadcastMessageId
                            + " and botSchemeId="
                            + botScheme.getId()
                            + " and tamBotId="
                            + tamBot.getId().getBotId()
                            + " and chatChannelId"
                            + chatChannel.getId().getChatId(),
                    Errors.BROADCAST_MESSAGE_DOES_NOT_EXIST
            );
        }
        return new BroadcastMessageSuccessResponse(broadcastMessage);
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
        return new BroadcastMessageListSuccessReponse(broadcastMessages);
    }

    public SuccessResponse removeBroadcastMessage(
            final String authToken,
            int botSchemeId,
            long chatChannelId,
            long broadcastMessageId
    ) {
        BotSchemeEntity botScheme = botSchemeService.getBotScheme(authToken, botSchemeId);
        TamBotEntity tamBot = tamBotService.getTamBot(botScheme);
        ChatChannelEntity chatChannel = chatChannelService.getChatChannel(botScheme, tamBot, chatChannelId);
        if (!broadcastMessageRepository
                .existsByBotSchemeIdAndTamBotIdAndChatChannelIdAndId(
                        botScheme.getId(),
                        tamBot.getId().getBotId(),
                        chatChannel.getId().getChatId(),
                        broadcastMessageId
                )) {
            throw new NotFoundEntityException(
                    "Can't find broadcastMessage with id="
                            + broadcastMessageId
                            + " and botSchemeId="
                            + botScheme.getId()
                            + " and tamBotId="
                            + tamBot.getId().getBotId()
                            + " and chatChannelId"
                            + chatChannel.getId().getChatId(),
                    Errors.BROADCAST_MESSAGE_DOES_NOT_EXIST
            );
        }
        broadcastMessageRepository
                .deleteByBotSchemeIdAndTamBotIdAndChatChannelIdAndId(
                        botScheme.getId(),
                        tamBot.getId().getBotId(),
                        chatChannel.getId().getChatId(),
                        broadcastMessageId
                );
        return new SuccessResponse();
    }

    public SuccessResponse addBroadcastMessage(
            final String authToken,
            int botSchemeId,
            long chatChannelId,
            final BroadcastMessageEntity broadcastMessage
    ) {
        if (broadcastMessage.getFiringTime() == null) {
            throw new CreateBroadcastMessageException(
                    "Can't create broadCastMessage cause firing time is null",
                    Errors.BROADCAST_MESSAGE_FIRING_TIME_IS_NULL
            );
        }
        LocalDateTime localDateTime = LocalDateTime.now();
        if (!localDateTime.isBefore(broadcastMessage.getFiringTime())) {
            throw new CreateBroadcastMessageException(
                    "Can't create broadCastMessage cause firing time in past="
                            + broadcastMessage.getFiringTime()
                            + " and local time="
                            + localDateTime,
                    Errors.BROADCAST_MESSAGE_FIRING_TIME_IS_IN_PAST
            );
        }
        BotSchemeEntity botScheme = botSchemeService.getBotScheme(authToken, botSchemeId);
        TamBotEntity tamBot = tamBotService.getTamBot(botScheme);
        ChatChannelEntity chatChannel = chatChannelService.getChatChannel(botScheme, tamBot, chatChannelId);
        broadcastMessage.setBotSchemeId(botScheme.getId());
        broadcastMessage.setTamBotId(tamBot.getId().getBotId());
        broadcastMessage.setChatChannelId(chatChannel.getId().getChatId());
        broadcastMessage.setState(BroadcastMessageState.SCHEDULED.getValue());
        return new BroadcastMessageSuccessResponse(broadcastMessageRepository.save(broadcastMessage));
    }
}
