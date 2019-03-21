package chat.tamtam.bot.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import chat.tamtam.botapi.model.Message;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping(path = Endpoint.TAM_BOT_WEBHOOK,
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.ALL_VALUE)
public class WebHookBotController {
    @PostMapping(Endpoint.ID)
    public ResponseEntity<?> webHookMessage(
            @PathVariable final String id,
            @RequestBody final Message message
    ) {
        //should always respond HttpStatus.OK
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
