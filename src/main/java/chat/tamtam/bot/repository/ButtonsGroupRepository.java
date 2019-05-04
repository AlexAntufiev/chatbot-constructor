package chat.tamtam.bot.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import chat.tamtam.bot.domain.builder.button.ButtonsGroup;

@Repository
public interface ButtonsGroupRepository extends CrudRepository<ButtonsGroup, Long> {
    boolean existsByIdAndAndComponentId(Long id, Long componentId);

    Optional<ButtonsGroup> findByComponentId(Long componentId);
}
