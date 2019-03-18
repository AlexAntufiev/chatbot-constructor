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

import chat.tamtam.bot.domain.chatchannel.SelectedChatChannelEntity;
import chat.tamtam.bot.service.ChatChannelService;
import chat.tamtam.bot.service.TamBotService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
@AllArgsConstructor
@RequestMapping(
        path = Endpoints.API_BOT,
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
)
public class BroadcastController {
    private final TamBotService tamBotService;
    private final ChatChannelService chatChannelService;

    @GetMapping(
            path = {
                    Endpoints.ID + Endpoints.TAM_CHATCHANNEL + Endpoints.ADMIN
                            + Endpoints.LIST + Endpoints.TAM_MARKER,
                    Endpoints.ID + Endpoints.TAM_CHATCHANNEL + Endpoints.ADMIN + Endpoints.LIST
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

    @PostMapping(Endpoints.ID + Endpoints.TAM_CHATCHANNEL + Endpoints.STORE)
    public ResponseEntity<?> storeChatChannel(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) final String authToken,
            @PathVariable("id") final Integer botSchemeId,
            @RequestBody final SelectedChatChannelEntity selectedChannel
    ) {
        return new ResponseEntity<>(
                chatChannelService.storeChatChannel(authToken, botSchemeId, selectedChannel),
                HttpStatus.OK
        );
    }

    @GetMapping(
            path = Endpoints.ID + Endpoints.TAM_CHATCHANNEL + Endpoints.LIST,
            consumes = MediaType.ALL_VALUE
    )
    public ResponseEntity<?> getChatChannels(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) final String authToken,
            @PathVariable("id") final Integer botSchemeId
    ) {
        return new ResponseEntity<>(
                chatChannelService.getChatChannels(authToken, botSchemeId),
                HttpStatus.OK
        );
    }

    @GetMapping(
            path = Endpoints.ID + Endpoints.TAM_CHATCHANNEL + Endpoints.CHATCHANNEL_ID,
            consumes = MediaType.ALL_VALUE
    )
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

    @PostMapping(
            path = Endpoints.ID + Endpoints.TAM_CHATCHANNEL + Endpoints.CHATCHANNEL_ID + Endpoints.DELETE,
            consumes = MediaType.ALL_VALUE
    )
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
}
