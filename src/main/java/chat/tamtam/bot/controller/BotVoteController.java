package chat.tamtam.bot.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import chat.tamtam.bot.service.BotVoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping(
        path = Endpoint.API_BOT + Endpoint.ID + Endpoint.VOTE,
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
)
public class BotVoteController {
    private final BotVoteService voteService;

    @GetMapping(path = Endpoint.LIST, consumes = MediaType.ALL_VALUE)
    public ResponseEntity<?> getVotesList(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) final String authToken,
            @PathVariable("id") final int botSchemeId
    ) {
        return ResponseEntity.ok(voteService.getVotes(authToken, botSchemeId));
    }
}
