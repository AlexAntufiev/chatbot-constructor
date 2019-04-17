package chat.tamtam.bot.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import chat.tamtam.bot.service.WebHookBotService;
import chat.tamtam.botapi.model.Update;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@AllArgsConstructor
@RestController
@RequestMapping(path = Endpoint.TAM_BOT,
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.ALL_VALUE)
public class WebHookBotController {
    private final WebHookBotService webHookBotService;

    @PostMapping(Endpoint.ID)
    public ResponseEntity<?> webHookMessage(
            @PathVariable final long botId,
            @RequestBody final Update update
    ) {
        try {
            webHookBotService.submit(botId, update);
        } catch (RuntimeException e) {
            //log
        } finally {
            return new ResponseEntity<>(HttpStatus.OK);
        }
    }
}
