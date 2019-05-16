package chat.tamtam.bot.controller;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
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
    public void webHookMessage(
            @PathVariable("id") final String botId,
            @RequestBody final Update update,
            final HttpServletResponse response
    ) {
        try {
            PrintWriter writer = response.getWriter();
            writer.write(HttpServletResponse.SC_OK);
            writer.flush();
            writer.close();
            webHookCustomBotService.submit(botId, update);
        } catch (IOException e) {
            log.error(
                    String.format(
                            "Response to custom bot webhook produced exception(botId=%s, update=%s)",
                            botId, update
                    ),
                    e
            );
        } catch (UnsupportedOperationException e) {
            log.error(String.format(
                    "Webhook service can not submit message: [%s] to bot with id = [%s]",
                    update,
                    botId
            ), e);
        }
    }
}
