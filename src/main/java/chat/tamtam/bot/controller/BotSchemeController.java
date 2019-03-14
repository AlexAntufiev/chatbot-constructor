package chat.tamtam.bot.controller;

import java.util.NoSuchElementException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import chat.tamtam.bot.domain.BotSchemeEntity;
import chat.tamtam.bot.domain.BotTokenEntity;
import chat.tamtam.bot.service.BotSchemeService;
import chat.tamtam.bot.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
@AllArgsConstructor
@RequestMapping(path = Endpoints.API_BOT, consumes = MediaType.APPLICATION_JSON_VALUE, produces =
        MediaType.APPLICATION_JSON_VALUE)
public class BotSchemeController {
    private final UserService userService;
    private final BotSchemeService botSchemeService;

    @GetMapping(path = Endpoints.LIST, consumes = MediaType.ALL_VALUE)
    public ResponseEntity<?> botList(@RequestHeader(name = HttpHeaders.AUTHORIZATION) final String authToken) {
        return new ResponseEntity<>(botSchemeService.getList(userService.getUserIdByToken(authToken)), HttpStatus.OK);
    }

    @GetMapping(path = Endpoints.ID, consumes = MediaType.ALL_VALUE)
    public ResponseEntity<?> getBotScheme(
            @PathVariable final int id, @RequestHeader(name = HttpHeaders.AUTHORIZATION) final String authToken
    ) {
        return new ResponseEntity<>(botSchemeService.getBotScheme(authToken, id), HttpStatus.OK);
    }

    @PostMapping(Endpoints.ADD)
    public ResponseEntity<?> addBot(
            @RequestBody final BotSchemeEntity bot,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) final String authToken
    ) {
        try {
            return new ResponseEntity<>(botSchemeService.addBot(bot, userService.getUserIdByToken(authToken)),
                    HttpStatus.OK
            );
        } catch (IllegalArgumentException e) {
            log.error("addBot {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping(Endpoints.DELETE)
    public ResponseEntity<?> deleteBot(
            @RequestBody final BotSchemeEntity bot,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) final String authToken
    ) {
        if (bot.getId() != null) {
            try {
                botSchemeService.deleteByUserIdAndId(userService.getUserIdByToken(authToken), bot.getId());
                return new ResponseEntity<>(HttpStatus.OK);
            } catch (NoSuchElementException e) {
                log.error("deleteBot {}", e.getMessage());
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        }
        log.error("invalid deleteBot request {}", bot);
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @PostMapping(Endpoints.ID_SAVE)
    public ResponseEntity<?> saveBot(
            @PathVariable final Integer id, @RequestBody final BotSchemeEntity bot,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) final String authToken
    ) {
        Integer userId = userService.getUserIdByToken(authToken);
        try {
            botSchemeService.saveBot(bot, userId, id);
        } catch (NoSuchElementException e) {
            log.error("saveBot {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping(Endpoints.ID + Endpoints.TAM_CONNECT)
    public ResponseEntity<?> connectBot(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) final String authToken,
            @PathVariable final Integer id,
            @RequestBody final BotTokenEntity tokenEntity
    ) {
        botSchemeService.connect(authToken, id, tokenEntity);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping(path = Endpoints.ID + Endpoints.TAM_DISCONNECT, consumes = MediaType.ALL_VALUE)
    public ResponseEntity<?> disconnectBot(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) final String authToken,
            @PathVariable final Integer id
    ) {
        botSchemeService.disconnect(authToken, id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
