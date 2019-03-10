package chat.tamtam.bot.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import chat.tamtam.bot.domain.BotSchemaEntity;

@Repository
public interface BotSchemaRepository extends CrudRepository<BotSchemaEntity, Integer> {
    List<BotSchemaEntity> findAllByUserId(Integer userId);

    BotSchemaEntity findByUserIdAndId(Integer userId, int id);

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    void deleteByUserIdAndId(Integer userId, Integer id);

    boolean existsByUserIdAndId(Integer userId, Integer id);
}
