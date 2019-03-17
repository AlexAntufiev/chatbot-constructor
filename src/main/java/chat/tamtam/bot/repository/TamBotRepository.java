package chat.tamtam.bot.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import chat.tamtam.bot.domain.TamBotEntity;

@Repository
public interface TamBotRepository extends CrudRepository<TamBotEntity, Integer> {
    TamBotEntity findById(TamBotEntity.TamBotId tamBotId);

    @Transactional
    void deleteById(TamBotEntity.TamBotId tamBotId);
}
