package chat.tamtam.bot.controller;

import chat.tamtam.bot.domain.UserEntity;
import chat.tamtam.bot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


@Controller
public class RestController {

    private final UserService userService;

    @Autowired
    RestController(final UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/api/registration")
    @ResponseBody
    public ResponseEntity<?> registration(@RequestBody UserEntity userEntity) {
        this.userService.addUser(userEntity);
        return new ResponseEntity<UserEntity>(userEntity, null, HttpStatus.OK);
    }

    @PostMapping(path = "/api/bot/add")
    @ResponseBody
    public ResponseEntity<?> addBot(@RequestBody final String body) {
        return new ResponseEntity<Object>(body, null, HttpStatus.OK);
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

    @GetMapping("/api/delete")
    @ResponseBody
    public ResponseEntity<?> deleteAllUsers() {
        this.userService.deleteAll();
        return new ResponseEntity<Object>(null, null, HttpStatus.OK);
    }
}
