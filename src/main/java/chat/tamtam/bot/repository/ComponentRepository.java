package chat.tamtam.bot.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import chat.tamtam.bot.domain.builder.component.Component;

@Repository
public interface ComponentRepository extends CrudRepository<Component, Long> {
    boolean existsByIdAndSchemeId(Long id, Integer schemeId);

    Iterable<Component> findAllBySchemeId(Integer schemeId);
}
