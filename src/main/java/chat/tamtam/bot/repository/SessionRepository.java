package chat.tamtam.bot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import chat.tamtam.bot.domain.session.SessionEntity;

@Repository
public interface SessionRepository extends JpaRepository<SessionEntity, Integer> {
    SessionEntity findByToken(String token);

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    void removeByToken(String token);

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    void removeAllByLogin(String login);
}
