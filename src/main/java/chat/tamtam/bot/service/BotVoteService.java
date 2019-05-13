package chat.tamtam.bot.service;

import org.springframework.stereotype.Service;

import chat.tamtam.bot.domain.response.SuccessResponse;
import chat.tamtam.bot.domain.response.SuccessResponseWrapper;
import chat.tamtam.bot.repository.BotVoteRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BotVoteService {
    private final BotVoteRepository voteRepository;

    private final BotSchemeService botSchemeService;

    public SuccessResponse getVotes(final String authToken, final int schemeId) {
        botSchemeService.getBotScheme(authToken, schemeId);
        return new SuccessResponseWrapper<>(voteRepository.findAllBySchemeId(schemeId));
    }
}
