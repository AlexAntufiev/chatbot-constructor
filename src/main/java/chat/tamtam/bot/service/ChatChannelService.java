package chat.tamtam.bot.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import chat.tamtam.bot.configuration.logging.Loggable;
import chat.tamtam.bot.domain.bot.BotSchemeEntity;
import chat.tamtam.bot.domain.bot.TamBotEntity;
import chat.tamtam.bot.domain.chatchannel.ChatChannelEntity;
import chat.tamtam.bot.domain.chatchannel.SelectedChatChannelEntity;
import chat.tamtam.bot.domain.exception.ChatBotConstructorException;
import chat.tamtam.bot.domain.exception.ChatChannelStoreException;
import chat.tamtam.bot.domain.exception.NotFoundEntityException;
import chat.tamtam.bot.domain.response.SuccessResponse;
import chat.tamtam.bot.domain.response.SuccessResponseListWrapper;
import chat.tamtam.bot.domain.response.SuccessResponseWrapper;
import chat.tamtam.bot.repository.ChatChannelRepository;
import chat.tamtam.botapi.TamTamBotAPI;
import chat.tamtam.botapi.exceptions.APIException;
import chat.tamtam.botapi.exceptions.ClientException;
import chat.tamtam.botapi.model.Chat;
import chat.tamtam.botapi.model.ChatList;
import chat.tamtam.botapi.model.ChatMember;
import chat.tamtam.botapi.model.ChatStatus;
import chat.tamtam.botapi.model.ChatType;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatChannelService {
    private final ChatChannelRepository chatChannelRepository;
    private final BotSchemeService botSchemeService;
    private final TamBotService tamBotService;

    @Value("${tamtam.chatChannel.listSizeThreshold:10}")
    private int listSizeThreshold;
    @Value("${tamtam.chatChannel.chatsFetchAmount:10}")
    private int chatChannelsFetchAmount;

    @Loggable
    public SuccessResponse getChatsWhereBotIsAdmin(
            final String authToken,
            int botSchemeId,
            final Long currentMarker
    ) {
        BotSchemeEntity botScheme = botSchemeService.getBotScheme(authToken, botSchemeId);
        TamBotEntity tamBot = tamBotService.getTamBot(botScheme);
        TamTamBotAPI tamTamBotAPI = TamTamBotAPI.create(tamBot.getToken());
        try {
            final List<Chat> tamChatEntities = new ArrayList<>();
            Long marker = currentMarker;
            while (tamChatEntities.size() < listSizeThreshold) {
                ChatList chatList = tamTamBotAPI
                        .getChats()
                        .marker(marker)
                        .count(chatChannelsFetchAmount)
                        .execute();
                marker = chatList.getMarker();
                chatList.getChats().forEach(chat -> {
                    if (chat.getOwnerId() != null
                            && chat.getStatus() == ChatStatus.ACTIVE
                            && chat.getType() == ChatType.CHANNEL) {

                        tamChatEntities.add(chat);
                    }
                });
                if (marker == null) {
                    break;
                }
            }
            return new SuccessResponseListWrapper<>(tamChatEntities, marker);
        } catch (ClientException | APIException e) {
            throw new ChatBotConstructorException(
                    "Can't fetch chatChannels where tam bot with id="
                            + botScheme.getBotId()
                            + " is participant",
                    Error.TAM_SERVICE_ERROR
            );
        }
    }

    @Loggable
    public SuccessResponse saveChatChannel(
            final String authToken,
            int botSchemeId,
            final SelectedChatChannelEntity selectedChatChannel
    ) {
        if (selectedChatChannel.getChatChannel() == null) {
            throw new ChatChannelStoreException(
                    "Empty chatChannel",
                    Error.CHATCHANNEL_SELECTED_EMPTY,
                    null
            );
        }
        BotSchemeEntity botScheme = botSchemeService.getBotScheme(authToken, botSchemeId);
        TamBotEntity tamBot = tamBotService.getTamBot(botScheme);
        TamTamBotAPI tamTamBotAPI = TamTamBotAPI.create(tamBot.getToken());
        try {
            Chat chat = tamTamBotAPI.getChat(selectedChatChannel.getChatChannel()).execute();
            ChatMember chatMember = tamTamBotAPI.getMembership(selectedChatChannel.getChatChannel()).execute();
            if (chat.getType() != ChatType.CHANNEL) {
                throw new ChatChannelStoreException(
                        "Can't store chatChannel with id="
                                + selectedChatChannel.getChatChannel()
                                + "cause it is not chatChannel",
                        Error.CHAT_NOT_CHANNEL,
                        selectedChatChannel.getChatChannel()
                );
            }
            if (chatMember.getPermissions() == null) {
                throw new ChatChannelStoreException(
                        "Can't store chatChannel with id="
                                + selectedChatChannel.getChatChannel()
                                + "cause tam bot with id="
                                + botScheme.getBotId()
                                + " has insufficient permissions",
                        Error.CHATCHANNEL_PERMISSIONS_ERROR,
                        selectedChatChannel.getChatChannel()
                );
            }
            ChatChannelEntity chatChannelEntity = new ChatChannelEntity(
                    botSchemeId,
                    botScheme.getBotId(),
                    chat
            );
            chatChannelEntity.setOptions(chat, chatMember);
            chatChannelRepository.save(chatChannelEntity);
            return new SuccessResponse();
        } catch (ClientException | APIException e) {
            throw new ChatChannelStoreException(
                    "Can't store chatChannel with id="
                            + selectedChatChannel.getChatChannel()
                            + " cause"
                            + e.getLocalizedMessage(),
                    Error.SERVICE_NO_ENTITY,
                    selectedChatChannel.getChatChannel()
            );
        }
    }

    @Loggable
    public SuccessResponse getChatChannels(
            final String authToken,
            int botSchemeId
    ) {
        BotSchemeEntity botScheme = botSchemeService.getBotScheme(authToken, botSchemeId);
        TamBotEntity tamBot = tamBotService.getTamBot(botScheme);
        return new SuccessResponseListWrapper<>(
                chatChannelRepository
                        .findAllByIdBotSchemeIdAndIdTamBotId(botScheme.getId(), tamBot.getId().getBotId()),
                null
        );
    }

    @Loggable
    public SuccessResponse getChatChannel(
            final String authToken,
            int botSchemeId,
            long chatId
    ) {
        BotSchemeEntity botScheme = botSchemeService.getBotScheme(authToken, botSchemeId);
        TamBotEntity tamBot = tamBotService.getTamBot(botScheme);
        ChatChannelEntity chatChannel = chatChannelRepository
                .findByIdBotSchemeIdAndIdTamBotIdAndIdChatId(
                        botScheme.getId(),
                        botScheme.getBotId(),
                        chatId
                );
        if (chatChannel == null) {
            throw new NotFoundEntityException(
                    "Can't find chatChannel for id="
                            + chatId
                            + " where botSchemeId="
                            + botScheme.getId()
                            + " and tamBotId="
                            + botScheme.getBotId(),
                    Error.CHATCHANNEL_DOES_NOT_EXIST
            );
        }
        return new SuccessResponseWrapper<>(chatChannel);
    }

    @Loggable
    public SuccessResponse removeChatChannel(
            final String authToken,
            int botSchemeId,
            long chatChannelId
    ) {
        BotSchemeEntity botScheme = botSchemeService.getBotScheme(authToken, botSchemeId);
        TamBotEntity tamBot = tamBotService.getTamBot(botScheme);
        if (!chatChannelRepository
                .existsByIdBotSchemeIdAndIdTamBotIdAndIdChatId(
                        botSchemeId,
                        tamBot.getId().getBotId(),
                        chatChannelId
                )) {
            throw new NotFoundEntityException(
                    "Can't delete chatchannel with id="
                            + chatChannelId
                            + " for botSchemeId="
                            + botSchemeId
                            + " and tamBotId="
                            + tamBot.getId().getBotId()
                            + " cause does not exist",
                    Error.CHATCHANNEL_DOES_NOT_EXIST
            );
        }
        chatChannelRepository
                .removeByIdBotSchemeIdAndIdTamBotIdAndIdChatId(
                        botScheme.getId(),
                        tamBot.getId().getBotId(),
                        chatChannelId
                );
        return new SuccessResponse();
    }

    @Loggable
    public ChatChannelEntity getChatChannel(
            final BotSchemeEntity botScheme,
            final TamBotEntity tamBot,
            long chatChannelId
    ) {
        ChatChannelEntity chatChannel =
                chatChannelRepository
                        .findByIdBotSchemeIdAndIdTamBotIdAndIdChatId(
                                botScheme.getId(),
                                tamBot.getId().getBotId(),
                                chatChannelId
                        );
        if (chatChannel == null) {
            throw new NotFoundEntityException(
                    "Can't find chatchannel with id="
                            + chatChannelId
                            + " and botSchemeId="
                            + botScheme.getId()
                            + " and tamBotId="
                            + tamBot.getId().getBotId(),
                    Error.CHATCHANNEL_DOES_NOT_EXIST
            );
        }
        return chatChannel;
    }
}
