package chat.tamtam.bot.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import chat.tamtam.bot.domain.BotSchemeEntity;
import chat.tamtam.bot.domain.TamBotEntity;
import chat.tamtam.bot.domain.chat.SelectedChatChannelEntity;
import chat.tamtam.bot.domain.chat.TamChatEntity;
import chat.tamtam.bot.domain.exception.ChatChannelStoreException;
import chat.tamtam.bot.domain.exception.NotFoundEntityException;
import chat.tamtam.bot.domain.exception.TamBotException;
import chat.tamtam.bot.domain.response.SuccessResponse;
import chat.tamtam.bot.domain.response.TamBotChatListResponse;
import chat.tamtam.bot.repository.TamChatChannelRepository;
import chat.tamtam.botapi.TamTamBotAPI;
import chat.tamtam.botapi.exceptions.APIException;
import chat.tamtam.botapi.exceptions.ClientException;
import chat.tamtam.botapi.model.Chat;
import chat.tamtam.botapi.model.ChatAdminPermission;
import chat.tamtam.botapi.model.ChatList;
import chat.tamtam.botapi.model.ChatMember;
import chat.tamtam.botapi.model.ChatStatus;
import chat.tamtam.botapi.model.ChatType;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TamChatChannelService {
    private final TamChatChannelRepository tamChatChannelRepository;
    private final BotSchemeService botSchemeService;
    private final TamBotService tamBotService;

    @Value("${tamtam.chat.listSizeTrashHold:10}")
    private int listSizeTrashHold;

    public TamBotChatListResponse getChatsWhereParticipant(
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
            while (tamChatEntities.size() < listSizeTrashHold) {
                ChatList chatList = tamTamBotAPI.getChats().marker(marker).count(1).execute();
                marker = chatList.getMarker();
                chatList.getChats().forEach(chat -> {
                    // @todo #CC-63 enable ownerId check when it will be available
                    if (/*chat.getOwnerId() != null
                            &&*/ chat.getStatus() == ChatStatus.ACTIVE
                            && chat.getType() == ChatType.CHANNEL) {

                        tamChatEntities.add(chat);
                    }
                });
                if (marker == null) {
                    break;
                }
            }
            return new TamBotChatListResponse(tamChatEntities, marker);
        } catch (ClientException | APIException e) {
            throw new TamBotException(
                    "Can't fetch chats where tam bot with id="
                            + botScheme.getBotId()
                            + "is participant",
                    Errors.TAM_SERVICE_ERROR
            );
        }
    }

    public SuccessResponse storeChannel(
            final String authToken,
            int botSchemeId,
            final SelectedChatChannelEntity selectedChannel
    ) {
        if (selectedChannel.getChat() == null) {
            throw new ChatChannelStoreException(
                    "Empty chat",
                    Errors.CHATS_SELECTED_EMPTY,
                    Collections.emptyList()
            );
        }
        BotSchemeEntity botScheme = botSchemeService.getBotScheme(authToken, botSchemeId);
        TamBotEntity tamBot = tamBotService.getTamBot(botScheme);
        TamTamBotAPI tamTamBotAPI = TamTamBotAPI.create(tamBot.getToken());
        try {
            Chat chat = tamTamBotAPI.getChat(selectedChannel.getChat()).execute();
            ChatMember chatMember = tamTamBotAPI.getMembership(selectedChannel.getChat()).execute();
            if (chat.getType() != ChatType.CHANNEL) {
                throw new ChatChannelStoreException(
                        "Can't store chat with id="
                                + selectedChannel.getChat()
                                + "cause it is not chat",
                        Errors.CHATS_NOT_CHANNEL,
                        Collections.singletonList(selectedChannel.getChat())
                );
            }
            if (chatMember.getPermissions() == null
                    || !chatMember.getPermissions().contains(ChatAdminPermission.WRITE)) {
                throw new ChatChannelStoreException(
                        "Can't store chat with id="
                                + selectedChannel.getChat()
                                + "cause tam bot with id="
                                + botScheme.getBotId()
                                + " has insufficient permissions",
                        Errors.CHATS_PERMISSIONS_ERROR,
                        Collections.singletonList(selectedChannel.getChat())
                );
            }
            TamChatEntity chatChannelEntity = new TamChatEntity(
                    botSchemeId,
                    botScheme.getBotId(),
                    chat
            );
            tamChatChannelRepository.save(chatChannelEntity);
            return new SuccessResponse();
        } catch (ClientException | APIException e) {
            throw new ChatChannelStoreException(
                    "Can't store chat with id="
                            + selectedChannel.getChat()
                            + " cause"
                            + e.getLocalizedMessage(),
                    Errors.SERVICE_ERROR,
                    Collections.emptyList()
            );
        }
    }

    public Iterable<TamChatEntity> getChannels(
            final String authToken,
            int botSchemeId
    ) {
        BotSchemeEntity botScheme = botSchemeService.getBotScheme(authToken, botSchemeId);
        TamBotEntity tamBot = tamBotService.getTamBot(botScheme);
        return tamChatChannelRepository
                .findAllByIdBotSchemeIdAndIdTamBotId(
                        botScheme.getId(),
                        tamBot.getId().getBotId()
                );
    }

    public TamChatEntity getChannel(
            final String authToken,
            int botSchemeId,
            long chatId
    ) {
        BotSchemeEntity botScheme = botSchemeService.getBotScheme(authToken, botSchemeId);
        TamBotEntity tamBot = tamBotService.getTamBot(botScheme);
        TamChatEntity tamChannel = tamChatChannelRepository
                .findByIdBotSchemeIdAndIdTamBotIdAndIdChatId(
                        botScheme.getId(),
                        botScheme.getBotId(),
                        chatId
                );
        if (tamChannel == null) {
            throw new NotFoundEntityException(
                    "Can't find chat for id="
                            + chatId
                            + " where botSchemeId="
                            + botScheme.getBotId()
                            + " and tamBotId="
                            + botScheme.getBotId()
            );
        }
        return tamChannel;
    }
}
