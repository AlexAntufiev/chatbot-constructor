package chat.tamtam.bot.repository;

import chat.tamtam.bot.domain.BotSchemaEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BotRepository extends CrudRepository<BotSchemaEntity, Integer> {
    List<BotSchemaEntity> findAllByUserId(Integer userId);
}
