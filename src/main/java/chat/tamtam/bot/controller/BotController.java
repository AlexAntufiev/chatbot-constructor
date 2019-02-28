package chat.tamtam.bot.controller;

import chat.tamtam.bot.security.SecurityConstants;
import chat.tamtam.bot.service.BotService;
import chat.tamtam.bot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
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

@RestController
@RequestMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
public class BotController {
    @Autowired
    private UserService userService;
    @Autowired
    private BotService botService;

    @GetMapping(path = Endpoints.API_BOT_LIST)
    public ResponseEntity<?> botList(@RequestHeader(name = SecurityConstants.HEADER_STRING) final String authToken) {
        return new ResponseEntity<>(
                botService.getList(userService.getUserIdByToken(authToken)),
                null,
                HttpStatus.OK);
    }

    @PostMapping(Endpoints.API_BOT_ADD)
    public ResponseEntity<?> addBot(@RequestBody final String body,
                                    @RequestHeader(name = SecurityConstants.HEADER_STRING) final String token) {
        return new ResponseEntity<>(null, null, HttpStatus.OK);
    }

    @PostMapping(Endpoints.API_BOT_DELETE)
    public ResponseEntity<?> deleteBot(@RequestBody final String body) {
        return new ResponseEntity<>(null, null, HttpStatus.OK);
    }

    @PostMapping(Endpoints.API_BOT_SAVE)
    public ResponseEntity<?> saveBot(@PathVariable final Integer id, @RequestBody final String body) {
        return new ResponseEntity<>(null, null, HttpStatus.OK);
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