package chat.tamtam.bot.repository;

import chat.tamtam.bot.domain.BotEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BotRepository extends CrudRepository<BotEntity, Integer> {
    List<BotEntity> findAllByUserId(Integer userId);
}
