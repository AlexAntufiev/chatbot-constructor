package chat.tamtam.bot.repository;

import chat.tamtam.bot.domain.SessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionRepository  extends JpaRepository<SessionEntity, Integer> {
    SessionEntity findByToken(String token);
}
