package chat.tamtam.bot.repository;

import chat.tamtam.bot.domain.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> {
    UserEntity findByUsername(String username);
    List<UserEntity> findUserEntitiesByUsername(String username);
}
