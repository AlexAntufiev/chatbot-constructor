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

import chat.tamtam.bot.domain.builder.component.ComponentUpdate;
import chat.tamtam.bot.domain.builder.component.group.SchemeComponentGroup;
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

    @GetMapping(path = Endpoint.SCHEME, consumes = MediaType.ALL_VALUE)
    public ResponseEntity<?> getBotScheme(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) final String authToken,
            @PathVariable("id") final int botSchemeId
    ) {
        return new ResponseEntity<>(
                builderService.getBotScheme(authToken, botSchemeId),
                HttpStatus.OK
        );
    }

    @PostMapping(path = Endpoint.SCHEME)
    public ResponseEntity<?> saveBotScheme(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) final String authToken,
            @PathVariable("id") final int botSchemeId,
            @RequestBody final List<ComponentUpdate> components
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

    @PostMapping(path = Endpoint.SCHEME + Endpoint.GROUP + Endpoint.ADD)
    public ResponseEntity<?> addSchemeGroup(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) final String authToken,
            @PathVariable("id") final int botSchemeId,
            @RequestBody final SchemeComponentGroup group
    ) {
        return ResponseEntity.ok(builderService.addSchemeGroup(authToken, botSchemeId, group));
    }

    @PostMapping(path = Endpoint.SCHEME + Endpoint.GROUP)
    public ResponseEntity<?> updateSchemeGroup(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) final String authToken,
            @PathVariable("id") final int botSchemeId,
            @RequestBody final SchemeComponentGroup group
    ) {
        return ResponseEntity.ok(builderService.updateSchemeGroup(authToken, botSchemeId, group));
    }

    @GetMapping(path = Endpoint.SCHEME + Endpoint.GROUP + Endpoint.GROUP_ID, consumes = MediaType.ALL_VALUE)
    public ResponseEntity<?> getSchemeGroup(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) final String authToken,
            @PathVariable("id") final int botSchemeId,
            @PathVariable("groupId") final long groupId
    ) {
        return ResponseEntity.ok(builderService.getSchemeGroup(authToken, botSchemeId, groupId));
    }
    
    @GetMapping(path = Endpoint.SCHEME + Endpoint.GROUP + Endpoint.LIST, consumes = MediaType.ALL_VALUE)
    public ResponseEntity<?> getSchemeGroups(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) final String authToken,
            @PathVariable("id") final int botSchemeId
    ) {
        return ResponseEntity.ok(builderService.getSchemeGroups(authToken, botSchemeId));
    }

    @PostMapping(path = Endpoint.SCHEME + Endpoint.GROUP + Endpoint.GROUP_ID + Endpoint.DELETE)
    public ResponseEntity<?> removeSchemeGroup(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) final String authToken,
            @PathVariable("id") final int botSchemeId,
            @PathVariable("groupId") final long groupId
    ) {
        return ResponseEntity.ok(builderService.removeSchemeGroup(authToken, botSchemeId, groupId));
    }
}
