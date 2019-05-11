package chat.tamtam.bot.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import chat.tamtam.bot.domain.bot.BotScheme;

@Repository
public interface BotSchemeRepository extends CrudRepository<BotScheme, Integer> {
    List<BotScheme> findAllByUserId(Long userId);

    BotScheme findByUserIdAndId(Long userId, int id);

    BotScheme findByBotId(Long botId);

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    void deleteByUserIdAndId(Long userId, Integer id);

    boolean existsByUserIdAndId(Long userId, Integer id);
}
