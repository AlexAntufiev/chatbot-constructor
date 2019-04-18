package chat.tamtam.bot.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import chat.tamtam.bot.domain.webhook.BotContext;

@Repository
public interface BotContextRepository extends CrudRepository<BotContext, Long> {
    Optional<BotContext> findByIdUserIdAndIdBotSchemeId(Long userId, Integer botId);
}
