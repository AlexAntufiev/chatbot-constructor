package chat.tamtam.bot.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import chat.tamtam.bot.domain.builder.action.SchemeAction;

@Repository
public interface SchemeActionRepository extends CrudRepository<SchemeAction, Long> {
    Iterable<SchemeAction> findAllByComponentIdOrderBySequence(Long componentId);

    Iterable<SchemeAction> findAllByComponentId(Long componentId);

    Optional<SchemeAction> findByIdAndComponentId(Long id, Long componentId);
}
