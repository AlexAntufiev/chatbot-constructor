package chat.tamtam.bot.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import chat.tamtam.bot.domain.builder.component.group.SchemeComponentGroup;

public interface ComponentGroupRepository extends CrudRepository<SchemeComponentGroup, Long> {
    Optional<SchemeComponentGroup> findByIdAndSchemeId(Long id, Integer schemeId);

    Iterable<SchemeComponentGroup> findAllBySchemeId(Integer schemeId);
}
