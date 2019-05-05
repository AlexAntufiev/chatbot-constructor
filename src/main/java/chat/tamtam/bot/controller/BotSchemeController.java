package chat.tamtam.bot.controller;

import java.util.NoSuchElementException;

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

import chat.tamtam.bot.domain.bot.BotScheme;
import chat.tamtam.bot.service.BotSchemeService;
import chat.tamtam.bot.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
@AllArgsConstructor
@RequestMapping(path = Endpoint.API_BOT, consumes = MediaType.APPLICATION_JSON_VALUE, produces =
        MediaType.APPLICATION_JSON_VALUE)
public class BotSchemeController {
    private final UserService userService;
    private final BotSchemeService botSchemeService;

    @GetMapping(path = Endpoint.LIST, consumes = MediaType.ALL_VALUE)
    public ResponseEntity<?> botList(@RequestHeader(name = HttpHeaders.AUTHORIZATION) final String authToken) {
        return new ResponseEntity<>(botSchemeService.getList(userService.getUserIdByToken(authToken)), HttpStatus.OK);
    }

    @GetMapping(path = Endpoint.ID, consumes = MediaType.ALL_VALUE)
    public ResponseEntity<?> getBotScheme(
            @PathVariable final int id, @RequestHeader(name = HttpHeaders.AUTHORIZATION) final String authToken
    ) {
        return new ResponseEntity<>(botSchemeService.getBotScheme(authToken, id), HttpStatus.OK);
    }

    @PostMapping(Endpoint.ADD)
    public ResponseEntity<?> addBot(
            @RequestBody final BotScheme bot,
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

    @PostMapping(path = Endpoint.ID + Endpoint.DELETE, consumes = MediaType.ALL_VALUE)
    public ResponseEntity<?> deleteBot(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) final String authToken,
            @PathVariable("id") final int botSchemeId
    ) {
        return new ResponseEntity<>(
                botSchemeService.deleteBot(authToken, botSchemeId),
                HttpStatus.OK
        );
    }

    @PostMapping(Endpoint.ID + Endpoint.SAVE)
    public ResponseEntity<?> saveBot(
            @PathVariable final Integer id, @RequestBody final BotScheme bot,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) final String authToken
    ) {
        Long userId = userService.getUserIdByToken(authToken);
        try {
            botSchemeService.saveBot(bot, userId, id);
        } catch (NoSuchElementException e) {
            log.error("saveBot {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
