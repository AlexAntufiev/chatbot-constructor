package chat.tamtam.bot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import chat.tamtam.bot.domain.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> {
    UserEntity findByLogin(String username);

    List<UserEntity> findUserEntitiesByLogin(String username);
}
