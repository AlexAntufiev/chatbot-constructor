package chat.tamtam.bot.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import chat.tamtam.bot.domain.vote.BotVote;

@Repository
public interface BotVoteRepository extends CrudRepository<BotVote, Long> {
    Iterable<BotVote> findAllBySchemeId(Integer schemeId);

    Iterable<BotVote> findAllBySchemeIdAndUserId(Integer schemeId, Long userId);

}
