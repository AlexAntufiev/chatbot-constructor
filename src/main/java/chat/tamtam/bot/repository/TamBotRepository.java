package chat.tamtam.bot.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import chat.tamtam.bot.domain.bot.TamBotEntity;

@Repository
public interface TamBotRepository extends CrudRepository<TamBotEntity, Integer> {
    TamBotEntity findById(TamBotEntity.Id id);

    boolean existsByIdBotId(long botId);

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    void deleteById(TamBotEntity.Id id);
}
