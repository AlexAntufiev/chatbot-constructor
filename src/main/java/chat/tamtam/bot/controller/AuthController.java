package chat.tamtam.bot.controller;

import chat.tamtam.bot.domain.UserAuthEntity;
import chat.tamtam.bot.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthController {
    private UserService userService;

    @PostMapping(Endpoints.API_REGISTRATION)
    public ResponseEntity<?> registration(@RequestBody final UserAuthEntity userAuthEntity) {
        boolean done = this.userService.addUser(userAuthEntity);
        if (done) {
            return new ResponseEntity<>(null, null, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(null, null, HttpStatus.BAD_REQUEST);
        }
    }
}
