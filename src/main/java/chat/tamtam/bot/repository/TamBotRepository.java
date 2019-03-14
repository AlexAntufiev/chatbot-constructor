package chat.tamtam.bot.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import chat.tamtam.bot.domain.TamBotEntity;
import chat.tamtam.bot.domain.TamBotId;

@Repository
public interface TamBotRepository extends CrudRepository<TamBotEntity, Integer> {
    TamBotEntity findById(TamBotId tamBotId);

    @Transactional
    void deleteById(TamBotId tamBotId);
}
