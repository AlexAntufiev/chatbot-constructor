package chat.tamtam.bot.controller;

import java.util.NoSuchElementException;

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

import chat.tamtam.bot.domain.BotSchemaEntity;
import chat.tamtam.bot.security.SecurityConstants;
import chat.tamtam.bot.service.BotService;
import chat.tamtam.bot.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
@AllArgsConstructor
@RequestMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
public class BotController {
    private final UserService userService;
    private final BotService botService;

    @GetMapping(path = Endpoints.API_BOT_LIST, consumes = MediaType.ALL_VALUE)
    public ResponseEntity<?> botList(@RequestHeader(name = SecurityConstants.HEADER_STRING) final String authToken) {
        return new ResponseEntity<>(
                botService.getList(userService.getUserIdByToken(authToken)),
                HttpStatus.OK
        );
    }

    @GetMapping(path = Endpoints.API_BOT_INFO, consumes = MediaType.ALL_VALUE)
    public ResponseEntity<?> getBotInfo(
            @PathVariable final Integer id,
            @RequestHeader(name = SecurityConstants.HEADER_STRING) final String authToken
    ) {
        try {
            return new ResponseEntity<>(
                    botService.getByUserIdAndId(userService.getUserIdByToken(authToken), id),
                    HttpStatus.OK
            );
        } catch (NoSuchElementException e) {
            log.error("getBotInfo {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping(Endpoints.API_BOT_ADD)
    public ResponseEntity<?> addBot(
            @RequestBody final BotSchemaEntity bot,
            @RequestHeader(name = SecurityConstants.HEADER_STRING) final String authToken
    ) {
        try {
            return new ResponseEntity<>(botService.addBot(bot, userService.getUserIdByToken(authToken)), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            log.error("addBot {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping(Endpoints.API_BOT_DELETE)
    public ResponseEntity<?> deleteBot(
            @RequestBody final BotSchemaEntity bot,
            @RequestHeader(name = SecurityConstants.HEADER_STRING) final String authToken
    ) {
        if (bot.getId() != null) {
            try {
                botService.deleteByUserIdAndId(userService.getUserIdByToken(authToken), bot.getId());
                return new ResponseEntity<>(HttpStatus.OK);
            } catch (NoSuchElementException e) {
                log.error("deleteBot {}", e.getMessage());
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        }
        log.error("invalid deleteBot request {}", bot);
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @PostMapping(Endpoints.API_BOT_SAVE)
    public ResponseEntity<?> saveBot(
            @PathVariable final Integer id,
            @RequestBody final BotSchemaEntity bot,
            @RequestHeader(name = SecurityConstants.HEADER_STRING) final String authToken
    ) {
        Integer userId = userService.getUserIdByToken(authToken);
        try {
            botService.saveBot(bot, userId, id);
        } catch (NoSuchElementException e) {
            log.error("saveBot {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping(Endpoints.API_BOT_CONNECT)
    public ResponseEntity<?> connectBot(@PathVariable final Integer id) {
        return new ResponseEntity<>(null, null, HttpStatus.OK);
    }

    @PostMapping(Endpoints.API_BOT_DISCONNECT)
    public ResponseEntity<?> disconnectBot(@PathVariable final Integer id) {
        return new ResponseEntity<>(null, null, HttpStatus.OK);
    }
}
