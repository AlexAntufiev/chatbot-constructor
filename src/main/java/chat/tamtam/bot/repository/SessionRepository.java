package chat.tamtam.bot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import chat.tamtam.bot.domain.SessionEntity;

@Repository
public interface SessionRepository extends JpaRepository<SessionEntity, Integer> {
    SessionEntity findByToken(String token);

    @Transactional
    void removeByToken(String token);

    @Transactional
    void removeAllByLogin(String login);
}
