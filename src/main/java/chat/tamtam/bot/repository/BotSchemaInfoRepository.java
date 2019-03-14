package chat.tamtam.bot.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import chat.tamtam.bot.domain.BotSchemaInfoEntity;

@Repository
public interface BotSchemaInfoRepository extends CrudRepository<BotSchemaInfoEntity, Integer> {
    BotSchemaInfoEntity findByBotId(Long botId);
}
