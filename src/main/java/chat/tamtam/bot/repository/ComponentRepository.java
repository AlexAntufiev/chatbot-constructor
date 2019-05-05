package chat.tamtam.bot.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import chat.tamtam.bot.domain.builder.component.BuilderComponent;

@Repository
public interface ComponentRepository extends CrudRepository<BuilderComponent, Long> {
    boolean existsByIdAndSchemeId(Long id, Integer schemeId);

    Iterable<BuilderComponent> findAllBySchemeId(Integer schemeId);
}
