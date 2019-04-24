package chat.tamtam.bot.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import chat.tamtam.bot.service.UserService;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping(path = Endpoints.RESOURCES, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
public class ResourcesController {
    private final ResourcesService resourcesService;

    @GetMapping(Endpoint.REGISTRATION_BOT_URL, consumes = MediaType.ALL_VALUE)
    public ResponseEntity<?> getRegistrationBotUrl() {
        return new ResponseEntity<>(resourceService.getRegistrationBotUrl(), HttpStatus.OK); 
    }
}
