package chat.tamtam.bot.controller;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import chat.tamtam.bot.domain.builder.component.Update;
import chat.tamtam.bot.service.BuilderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping(
        path = Endpoint.API_BOT + Endpoint.ID + Endpoint.BUILDER,
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
)
public class BuilderController {
    private final BuilderService builderService;

    @GetMapping(path = Endpoint.COMPONENT, consumes = MediaType.ALL_VALUE)
    public ResponseEntity<?> getNewComponentId(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) final String authToken,
            @PathVariable("id") final int botSchemeId
    ) {
        return new ResponseEntity<>(
                builderService.getNewComponentId(authToken, botSchemeId),
                HttpStatus.OK
        );
    }

    @PostMapping(path = Endpoint.SCHEME)
    public ResponseEntity<?> saveBotScheme(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) final String authToken,
            @PathVariable("id") final int botSchemeId,
            @RequestBody final List<Update> components
    ) {
        return new ResponseEntity<>(
                builderService.saveBotScheme(
                        authToken,
                        botSchemeId,
                        components
                ),
                HttpStatus.OK
        );
    }
}
