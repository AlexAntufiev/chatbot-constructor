package chat.tamtam.bot.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import chat.tamtam.bot.service.WebHookCustomBotService;
import chat.tamtam.botapi.model.Update;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@AllArgsConstructor
@RestController
@RequestMapping(path = Endpoint.TAM_CUSTOM_BOT_WEBHOOK,
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.ALL_VALUE)
public class WebHookCustomBotController {
    private final WebHookCustomBotService webHookCustomBotService;

    @PostMapping(Endpoint.ID)
    public ResponseEntity<?> webHookMessage(
            @PathVariable final String id,
            @RequestBody final Update update
    ) {
        try {
            webHookCustomBotService.submit(id, update);
        } catch (UnsupportedOperationException e) {
            log.error(String.format(
                    "Webhook service can not submit message: [%s] to bot with id = [%s]",
                    update,
                    id
            ), e);
        } finally {
            return new ResponseEntity<>(HttpStatus.OK);
        }
    }
}
