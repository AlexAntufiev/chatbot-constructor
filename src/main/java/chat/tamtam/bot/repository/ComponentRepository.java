package chat.tamtam.bot.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import chat.tamtam.bot.domain.builder.component.SchemeComponent;

@Repository
public interface ComponentRepository extends CrudRepository<SchemeComponent, Long> {
    boolean existsByIdAndSchemeId(Long id, Integer schemeId);

    //    Iterable<SchemeComponent> findAllBySchemeId(Integer schemeId);

    Iterable<SchemeComponent> findAllBySchemeIdOrderBySequence(Integer schemeId);
}
