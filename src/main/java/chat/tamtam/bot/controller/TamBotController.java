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

import chat.tamtam.bot.domain.bot.BotTokenEntity;
import chat.tamtam.bot.service.TamBotService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
@AllArgsConstructor
@RequestMapping(path = Endpoints.API_BOT, consumes = MediaType.APPLICATION_JSON_VALUE, produces =
        MediaType.APPLICATION_JSON_VALUE)
public class TamBotController {
    private final TamBotService tamBotService;

    @GetMapping(path = Endpoints.ID + Endpoints.STATUS, consumes = MediaType.ALL_VALUE)
    public ResponseEntity<?> status(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) final String authToken,
            @PathVariable("id") final int botSchemeId
    ) {
        return new ResponseEntity<>(
                tamBotService.status(authToken, botSchemeId),
                HttpStatus.OK
        );
    }

    @PostMapping(Endpoints.ID + Endpoints.TAM_CONNECT)
    public ResponseEntity<?> connectBot(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) final String authToken,
            @PathVariable("id") final Integer botSchemeId,
            @RequestBody final BotTokenEntity tokenEntity
    ) {
        return new ResponseEntity<>(
                tamBotService.connect(authToken, botSchemeId, tokenEntity.getToken()),
                HttpStatus.OK
        );
    }

    @PostMapping(path = Endpoints.ID + Endpoints.TAM_DISCONNECT, consumes = MediaType.ALL_VALUE)
    public ResponseEntity<?> disconnectBot(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) final String authToken,
            @PathVariable("id") final Integer botSchemeId
    ) {
        return new ResponseEntity<>(
                tamBotService.disconnect(authToken, botSchemeId),
                HttpStatus.OK
        );
    }
}
