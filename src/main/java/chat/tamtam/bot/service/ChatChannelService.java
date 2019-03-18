package chat.tamtam.bot.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import chat.tamtam.bot.domain.bot.BotSchemeEntity;
import chat.tamtam.bot.domain.bot.TamBotEntity;
import chat.tamtam.bot.domain.chatchannel.ChatChannelEntity;
import chat.tamtam.bot.domain.chatchannel.SelectedChatChannelEntity;
import chat.tamtam.bot.domain.exception.ChatChannelStoreException;
import chat.tamtam.bot.domain.exception.NotFoundEntityException;
import chat.tamtam.bot.domain.exception.TamBotException;
import chat.tamtam.bot.domain.response.ChatChannelListSuccessResponse;
import chat.tamtam.bot.domain.response.ChatChannelSuccessResponse;
import chat.tamtam.bot.domain.response.SuccessResponse;
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

    public ChatChannelListSuccessResponse getChatsWhereBotIsAdmin(
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
                    // @todo #CC-63 enable ownerId check when it will be available
                    if (/*chatChannel.getOwnerId() != null
                            &&*/ chat.getStatus() == ChatStatus.ACTIVE
                            && chat.getType() == ChatType.CHANNEL) {

                        tamChatEntities.add(chat);
                    }
                });
                if (marker == null) {
                    break;
                }
            }
            return new ChatChannelListSuccessResponse<>(tamChatEntities, marker);
        } catch (ClientException | APIException e) {
            throw new TamBotException(
                    "Can't fetch chatChannels where tam bot with id="
                            + botScheme.getBotId()
                            + "is participant",
                    Errors.TAM_SERVICE_ERROR
            );
        }
    }

    public SuccessResponse storeChatChannel(
            final String authToken,
            int botSchemeId,
            final SelectedChatChannelEntity selectedChatChannel
    ) {
        if (selectedChatChannel.getChatChannel() == null) {
            throw new ChatChannelStoreException(
                    "Empty chatChannel",
                    Errors.CHATCHANNEL_SELECTED_EMPTY,
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
                        Errors.CHAT_NOT_CHANNEL,
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
                        Errors.CHATCHANNEL_PERMISSIONS_ERROR,
                        selectedChatChannel.getChatChannel()
                );
            }
            ChatChannelEntity chatChannelEntity = new ChatChannelEntity(
                    botSchemeId,
                    botScheme.getBotId(),
                    chat
            );
            chatChannelRepository.save(chatChannelEntity);
            return new SuccessResponse();
        } catch (ClientException | APIException e) {
            throw new ChatChannelStoreException(
                    "Can't store chatChannel with id="
                            + selectedChatChannel.getChatChannel()
                            + " cause"
                            + e.getLocalizedMessage(),
                    Errors.SERVICE_ERROR,
                    selectedChatChannel.getChatChannel()
            );
        }
    }

    public ChatChannelListSuccessResponse getChatChannels(
            final String authToken,
            int botSchemeId
    ) {
        BotSchemeEntity botScheme = botSchemeService.getBotScheme(authToken, botSchemeId);
        TamBotEntity tamBot = tamBotService.getTamBot(botScheme);
        return new ChatChannelListSuccessResponse<>(
                chatChannelRepository
                        .findAllByIdBotSchemeIdAndIdTamBotId(botScheme.getId(), tamBot.getId().getBotId()),
                null
        );
    }

    public ChatChannelSuccessResponse getChatChannel(
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
                    Errors.CHATCHANNEL_NOT_EXIST
            );
        }
        return new ChatChannelSuccessResponse(chatChannel);
    }
}
