package chat.tamtam.bot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import chat.tamtam.bot.domain.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> {
    UserEntity findByLogin(String login);

    List<UserEntity> findUserEntitiesByLogin(String login);

    @Transactional
    void removeByLogin(String login);
}
