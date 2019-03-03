package chat.tamtam.bot.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import chat.tamtam.bot.domain.BotSchemaEntity;

@Repository
public interface BotSchemaRepository extends CrudRepository<BotSchemaEntity, Integer> {
    List<BotSchemaEntity> findAllByUserId(Integer userId);
}
