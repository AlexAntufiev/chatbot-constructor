package chat.tamtam.bot.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import chat.tamtam.bot.domain.BotSchemeEntity;
import chat.tamtam.bot.domain.TamBotEntity;
import chat.tamtam.bot.domain.channel.SelectedChannelEntity;
import chat.tamtam.bot.domain.channel.TamChannelEntity;
import chat.tamtam.bot.domain.exception.ChannelStoreException;
import chat.tamtam.bot.domain.exception.NotFoundEntityException;
import chat.tamtam.bot.domain.exception.TamBotException;
import chat.tamtam.bot.domain.response.SuccessResponse;
import chat.tamtam.bot.domain.response.TamBotChatsListResponse;
import chat.tamtam.bot.domain.response.TamChatResponse;
import chat.tamtam.bot.repository.TamChannelRepository;
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
public class TamChannelService {
    private final TamChannelRepository tamChannelRepository;
    private final BotSchemeService botSchemeService;
    private final TamBotService tamBotService;

    @Value("${tamtam.channel.listSizeTrashHold:10}")
    private int listSizeTrashHold;

    public TamBotChatsListResponse channelsList(final String authToken, int botSchemeId, final Long currentMarker) {
        BotSchemeEntity botScheme = botSchemeService.getBotScheme(authToken, botSchemeId);
        TamBotEntity tamBot = tamBotService.getTamBot(botScheme);
        TamTamBotAPI tamTamBotAPI = TamTamBotAPI.create(tamBot.getToken());
        try {
            final List<TamChatResponse> tamChatEntities = new ArrayList<>();
            Long marker = currentMarker;
            while (true) {
                ChatList chatList = tamTamBotAPI.getChats().marker(marker).count(1).execute();
                marker = chatList.getMarker();
                chatList.getChats().forEach(channel -> {
                    // @todo #CC-63 enable ownerId check when it will be available
                    if (/*channel.getOwnerId() != null
                            &&*/ channel.getStatus() == ChatStatus.ACTIVE
                            && channel.getType() == ChatType.CHANNEL) {

                        tamChatEntities.add(new TamChatResponse(channel));
                    }
                });
                if (tamChatEntities.size() >= listSizeTrashHold || marker == null) {
                    break;
                }
            }
            return new TamBotChatsListResponse(tamChatEntities, marker);
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
            final SelectedChannelEntity selectedChannel
    ) {
        if (selectedChannel.getChannel() == null) {
            throw new ChannelStoreException(
                    "Empty channel",
                    Errors.CHANNELS_SELECTED_EMPTY,
                    Collections.emptyList()
            );
        }
        BotSchemeEntity botScheme = botSchemeService.getBotScheme(authToken, botSchemeId);
        TamBotEntity tamBot = tamBotService.getTamBot(botScheme);
        TamTamBotAPI tamTamBotAPI = TamTamBotAPI.create(tamBot.getToken());
        try {
            Chat chat = tamTamBotAPI.getChat(selectedChannel.getChannel()).execute();
            ChatMember chatMember = tamTamBotAPI.getMembership(selectedChannel.getChannel()).execute();
            if (chatMember.getPermissions() == null
                    || !chatMember.getPermissions().contains(ChatAdminPermission.WRITE)) {
                throw new ChannelStoreException(
                        "Can't store channel with id="
                                + selectedChannel.getChannel()
                                + "cause tam bot with id="
                                + botScheme.getBotId()
                                + " has insufficient permissions",
                        Errors.CHANNELS_PERMISSIONS_ERROR,
                        Collections.singletonList(selectedChannel.getChannel())
                );
            }
            TamChannelEntity channel = new TamChannelEntity(
                    botSchemeId,
                    botScheme.getBotId(),
                    chat
            );
            tamChannelRepository.save(channel);
            return new SuccessResponse();
        } catch (ClientException | APIException e) {
            throw new ChannelStoreException(
                    "Can't store channel with id="
                            + selectedChannel.getChannel()
                            + " cause"
                            + e.getLocalizedMessage(),
                    Errors.SERVICE_ERROR,
                    Collections.emptyList()
            );
        }
    }

    public Iterable<TamChannelEntity> getChannels(
            final String authToken,
            int botSchemeId
    ) {
        BotSchemeEntity botScheme = botSchemeService.getBotScheme(authToken, botSchemeId);
        TamBotEntity tamBot = tamBotService.getTamBot(botScheme);
        return tamChannelRepository
                .findAllByChannelId_BotSchemeIdAndChannelId_TamBotId(
                        botScheme.getId(),
                        tamBot.getId().getBotId()
                );
    }

    public TamChannelEntity getChannel(
            final String authToken,
            int botSchemeId,
            long channelId
    ) {
        BotSchemeEntity botScheme = botSchemeService.getBotScheme(authToken, botSchemeId);
        TamBotEntity tamBot = tamBotService.getTamBot(botScheme);
        TamChannelEntity tamChannel = tamChannelRepository
                .findByChannelId_BotSchemeIdAndChannelId_TamBotIdAndChannelId_ChatId(
                        botScheme.getId(),
                        botScheme.getBotId(),
                        channelId
                );
        if (tamChannel == null) {
            throw new NotFoundEntityException(
                    "Can't find channel for id="
                            + channelId
                            + " where botSchemeId="
                            + botScheme.getBotId()
                            + " and tamBotId="
                            + botScheme.getBotId()
            );
        }
        return tamChannel;
    }
}
