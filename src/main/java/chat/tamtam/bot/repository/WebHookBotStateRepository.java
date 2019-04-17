package chat.tamtam.bot.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import chat.tamtam.bot.domain.webhook.WebHookBotState;

@Repository
public interface WebHookBotStateRepository extends CrudRepository<WebHookBotState, Long> {
    Optional<WebHookBotState> findByIdUserIdAndIdBotId(Long userId, Long botId);
}
