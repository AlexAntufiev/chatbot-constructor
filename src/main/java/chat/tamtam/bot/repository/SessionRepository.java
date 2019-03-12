package chat.tamtam.bot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import chat.tamtam.bot.domain.SessionEntity;

@Repository
@Transactional
public interface SessionRepository extends JpaRepository<SessionEntity, Integer> {
    SessionEntity findByToken(String token);

    void removeByToken(String token);
}
