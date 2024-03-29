package chat.tamtam.bot.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import chat.tamtam.bot.domain.builder.validator.ComponentValidator;

@Repository
public interface ComponentValidatorRepository extends CrudRepository<ComponentValidator, Long> {
    Iterable<ComponentValidator> findAllByComponentId(Long componentId);
}
