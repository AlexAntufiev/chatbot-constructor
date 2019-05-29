package chat.tamtam.bot.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import chat.tamtam.bot.domain.user.UserAuthEntity;
import chat.tamtam.bot.service.UserService;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthController {
    private final UserService userService;

    @PostMapping(Endpoint.API_REGISTRATION)
    public ResponseEntity<?> registration(@RequestBody final UserAuthEntity userAuthEntity) {
        boolean done = userService.addUser(userAuthEntity);
        if (done) {
            return new ResponseEntity<>(null, null, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(null, null, HttpStatus.BAD_REQUEST);
        }
    }
}
