package chat.tamtam.bot.controller;

import chat.tamtam.bot.domain.UserEntity;
import chat.tamtam.bot.security.SecurityConstants;
import chat.tamtam.bot.service.BotService;
import chat.tamtam.bot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@Controller
public class RestController {
    @Autowired
    private UserService userService;
    @Autowired
    private BotService botService;

    @PostMapping("/api/registration")
    @ResponseBody
    public ResponseEntity<?> registration(@RequestBody final UserEntity userEntity) {
        boolean done = this.userService.addUser(userEntity);
        if (done) {
            return new ResponseEntity<>(null, null, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(null, null, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/api/bot/list")
    @ResponseBody
    public ResponseEntity<?> botList(@RequestHeader(name = SecurityConstants.HEADER_STRING) final String token) {
        return new ResponseEntity<>(
                botService.getList(userService.getUserIdByToken(token)),
                null,
                HttpStatus.OK
        );
    }

    @PostMapping("/api/bot/add")
    @ResponseBody
    public ResponseEntity<?> addBot(@RequestBody final String body,
                                    @RequestHeader(name = SecurityConstants.HEADER_STRING) final String token) {
        return new ResponseEntity<Object>(null, null, HttpStatus.OK);
    }

    @GetMapping("/api/bot/delete")
    @ResponseBody
    public ResponseEntity<?> deleteBot(@RequestBody final String body) {
        return new ResponseEntity<Object>(null, null, HttpStatus.OK);
    }

    @PostMapping("/api/bot/{id}/save")
    @ResponseBody
    public ResponseEntity<?> saveBot(@PathVariable("id") final Integer id, @RequestBody final String body) {
        return new ResponseEntity<Object>(null, null, HttpStatus.OK);
    }

    @GetMapping("/api/bot/{id}/connect")
    @ResponseBody
    public ResponseEntity<?> connectBot(@PathVariable("id") final Integer id) {
        return new ResponseEntity<Object>(null, null, HttpStatus.OK);
    }

    @GetMapping("/api/bot/{id}/disconnect")
    @ResponseBody
    public ResponseEntity<?> disconnectBot(@PathVariable("id") final Integer id) {
        return new ResponseEntity<Object>(null, null, HttpStatus.OK);
    }
}
