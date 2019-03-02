package chat.tamtam.bot.controller;

import chat.tamtam.bot.security.SecurityConstants;
import chat.tamtam.bot.service.BotService;
import chat.tamtam.bot.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
public class BotController {
    private final UserService userService;
    private final BotService botService;

    @GetMapping(Endpoints.API_BOT_LIST)
    public ResponseEntity<?> botList(@RequestHeader(name = SecurityConstants.HEADER_STRING) final String authToken) {
        return new ResponseEntity<>(botService.getList(userService.getUserIdByToken(authToken)), HttpStatus.OK);
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
