package chat.tamtam.bot.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import chat.tamtam.bot.domain.broadcast.message.BroadcastMessageUpdate;
import chat.tamtam.bot.domain.chatchannel.SelectedChatChannelEntity;
import chat.tamtam.bot.service.BroadcastMessageService;
import chat.tamtam.bot.service.ChatChannelService;
import chat.tamtam.bot.service.TamBotService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
@AllArgsConstructor
@RequestMapping(
        path = Endpoint.API_BOT + Endpoint.ID + Endpoint.TAM_CHATCHANNEL,
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
)
public class BroadcastController {
    private final TamBotService tamBotService;
    private final ChatChannelService chatChannelService;
    private final BroadcastMessageService broadcastMessageService;

    @GetMapping(
            path = {
                    Endpoint.ADMIN + Endpoint.LIST + Endpoint.TAM_MARKER,
                    Endpoint.ADMIN + Endpoint.LIST
            },
            consumes = MediaType.ALL_VALUE
    )
    public ResponseEntity<?> getChatChannelsWhereBotIsAdmin(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) final String authToken,
            @PathVariable("id") final Integer botSchemeId,
            @PathVariable(required = false) final Long marker
    ) {
        return new ResponseEntity<>(
                chatChannelService.getChatsWhereBotIsAdmin(authToken, botSchemeId, marker),
                HttpStatus.OK
        );
    }

    @PostMapping(Endpoint.SAVE)
    public ResponseEntity<?> saveChatChannel(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) final String authToken,
            @PathVariable("id") final Integer botSchemeId,
            @RequestBody final SelectedChatChannelEntity selectedChannel
    ) {
        return new ResponseEntity<>(
                chatChannelService.saveChatChannel(authToken, botSchemeId, selectedChannel),
                HttpStatus.OK
        );
    }

    @GetMapping(path = Endpoint.LIST, consumes = MediaType.ALL_VALUE)
    public ResponseEntity<?> getChatChannels(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) final String authToken,
            @PathVariable("id") final Integer botSchemeId
    ) {
        return new ResponseEntity<>(
                chatChannelService.getChatChannels(authToken, botSchemeId),
                HttpStatus.OK
        );
    }

    @GetMapping(path = Endpoint.CHATCHANNEL_ID, consumes = MediaType.ALL_VALUE)
    public ResponseEntity<?> getChatChannel(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) final String authToken,
            @PathVariable("id") final Integer botSchemeId,
            @PathVariable("chatchannel_id") final Long channelId
    ) {
        return new ResponseEntity<>(
                chatChannelService.getChatChannel(authToken, botSchemeId, channelId),
                HttpStatus.OK
        );
    }

    @PostMapping(path = Endpoint.CHATCHANNEL_ID + Endpoint.DELETE, consumes = MediaType.ALL_VALUE)
    public ResponseEntity<?> removeChatChannel(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) final String authToken,
            @PathVariable("id") final Integer botSchemeId,
            @PathVariable("chatchannel_id") final Long channelId
    ) {
        return new ResponseEntity<>(
                chatChannelService.removeChatChannel(authToken, botSchemeId, channelId),
                HttpStatus.OK
        );
    }

    @PostMapping(path = Endpoint.CHATCHANNEL_ID + Endpoint.MESSAGE)
    public ResponseEntity<?> addBroadcastMessage(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) final String authToken,
            @PathVariable("id") final Integer botSchemeId,
            @PathVariable("chatchannel_id") final Long channelId,
            @RequestBody final BroadcastMessageUpdate broadcastMessageUpdate
    ) {
        return new ResponseEntity<>(
                broadcastMessageService
                        .addBroadcastMessage(authToken, botSchemeId, channelId, broadcastMessageUpdate),
                HttpStatus.OK
        );
    }

    @PostMapping(Endpoint.CHATCHANNEL_ID + Endpoint.MESSAGE + Endpoint.MESSAGE_ID)
    public ResponseEntity<?> updateBroadcastMessage(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) final String authToken,
            @PathVariable("id") final int botSchemeId,
            @PathVariable("chatchannel_id") final long channelId,
            @PathVariable("message_id") long messageId,
            @RequestBody final BroadcastMessageUpdate broadcastMessageUpdate
    ) {
        return new ResponseEntity<>(
                broadcastMessageService.updateBroadcastMessage(
                        authToken,
                        botSchemeId,
                        channelId,
                        messageId,
                        broadcastMessageUpdate
                ),
                HttpStatus.OK
        );
    }

    @GetMapping(
            path = Endpoint.CHATCHANNEL_ID + Endpoint.MESSAGE + Endpoint.MESSAGE_ID,
            consumes = MediaType.ALL_VALUE
    )
    public ResponseEntity<?> getBroadcastMessage(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) final String authToken,
            @PathVariable("id") final Integer botSchemeId,
            @PathVariable("chatchannel_id") final Long chatchannelId,
            @PathVariable("message_id") long messageId
    ) {
        return new ResponseEntity<>(
                broadcastMessageService.getBroadcastMessage(authToken, botSchemeId, chatchannelId, messageId),
                HttpStatus.OK
        );
    }

    @GetMapping(
            path = Endpoint.CHATCHANNEL_ID + Endpoint.MESSAGE + Endpoint.LIST,
            consumes = MediaType.ALL_VALUE
    )
    public ResponseEntity<?> getBroadcastMessages(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) final String authToken,
            @PathVariable("id") final Integer botSchemeId,
            @PathVariable("chatchannel_id") final Long channelId
    ) {
        return new ResponseEntity<>(
                broadcastMessageService.getBroadcastMessages(authToken, botSchemeId, channelId),
                HttpStatus.OK
        );
    }

    @PostMapping(
            path = Endpoint.CHATCHANNEL_ID + Endpoint.MESSAGE + Endpoint.MESSAGE_ID + Endpoint.DELETE,
            consumes = MediaType.ALL_VALUE
    )
    public ResponseEntity<?> removeBroadcastMessage(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) final String authToken,
            @PathVariable("id") final Integer botSchemeId,
            @PathVariable("chatchannel_id") final Long channelId,
            @PathVariable("message_id") long messageId
    ) {
        return new ResponseEntity<>(
                broadcastMessageService.removeBroadcastMessage(authToken, botSchemeId, channelId, messageId),
                HttpStatus.OK
        );
    }
}
