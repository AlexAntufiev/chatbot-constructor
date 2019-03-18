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

import chat.tamtam.bot.domain.chat.SelectedChatChannelEntity;
import chat.tamtam.bot.service.TamBotService;
import chat.tamtam.bot.service.TamChatChannelService;
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
    private final TamChatChannelService tamChatChannelService;

    @GetMapping(
            path = {
                    Endpoints.ID + Endpoints.TAM_CHANNELS + Endpoints.PARTICIPANT
                            + Endpoints.LIST + Endpoints.TAM_MARKER,
                    Endpoints.ID + Endpoints.TAM_CHANNELS + Endpoints.PARTICIPANT + Endpoints.LIST
            },
            consumes = MediaType.ALL_VALUE
    )
    public ResponseEntity<?> getTamChatsWhereParticipant(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) final String authToken,
            @PathVariable("id") final Integer botSchemeId,
            @PathVariable(required = false) final Long marker
    ) {
        return new ResponseEntity<>(
                tamChatChannelService.getChatsWhereParticipant(authToken, botSchemeId, marker),
                HttpStatus.OK
        );
    }

    @PostMapping(Endpoints.ID + Endpoints.TAM_CHANNELS + Endpoints.STORE)
    public ResponseEntity<?> storeChannel(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) final String authToken,
            @PathVariable("id") final Integer botSchemeId,
            @RequestBody final SelectedChatChannelEntity selectedChannel
    ) {
        return new ResponseEntity<>(
                tamChatChannelService.storeChannel(authToken, botSchemeId, selectedChannel),
                HttpStatus.OK
        );
    }

    @GetMapping(
            path = Endpoints.ID + Endpoints.TAM_CHANNELS + Endpoints.LIST,
            consumes = MediaType.ALL_VALUE
    )
    public ResponseEntity<?> getChannels(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) final String authToken,
            @PathVariable("id") final Integer botSchemeId
    ) {
        return new ResponseEntity<>(
                tamChatChannelService.getChannels(authToken, botSchemeId),
                HttpStatus.OK
        );
    }

    @GetMapping(
            path = Endpoints.ID + Endpoints.TAM_CHANNELS + Endpoints.CHANNEL_ID,
            consumes = MediaType.ALL_VALUE
    )
    public ResponseEntity<?> getChannel(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) final String authToken,
            @PathVariable("id") final Integer botSchemeId,
            @PathVariable("chat_id") final Long channelId
    ) {
        return new ResponseEntity<>(
                tamChatChannelService.getChannel(authToken, botSchemeId, channelId),
                HttpStatus.OK
        );
    }
}
