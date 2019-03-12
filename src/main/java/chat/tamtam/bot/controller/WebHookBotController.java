package chat.tamtam.bot.controller;

import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import chat.tamtam.bot.domain.webhook.WebHookMessageEntity;
import chat.tamtam.bot.service.WebHookBotService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@AllArgsConstructor
@RestController
@RequestMapping(path = Endpoints.TAM_BOT_WEBHOOK,
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.ALL_VALUE)
public class WebHookBotController {
    private final WebHookBotService webHookBotService;

    @PostMapping(Endpoints.TAM_BOT_ID)
    public ResponseEntity<?> webHookMessage(
            @PathVariable final String id,
            @RequestBody final WebHookMessageEntity message
    ) {
        try {
            webHookBotService.submit(id, message);
        } catch (NoSuchElementException e) {
            log.error("webHookMessage {}", e.getMessage());
        } finally {
            return new ResponseEntity<>(HttpStatus.OK);
        }
    }
}
