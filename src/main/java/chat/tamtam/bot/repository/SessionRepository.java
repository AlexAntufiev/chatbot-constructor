package chat.tamtam.bot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import chat.tamtam.bot.domain.SessionEntity;

@Repository
public interface SessionRepository extends JpaRepository<SessionEntity, Integer> {
    SessionEntity findByToken(String token);
}
