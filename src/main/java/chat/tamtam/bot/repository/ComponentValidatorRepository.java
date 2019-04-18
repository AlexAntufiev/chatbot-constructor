package chat.tamtam.bot.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import chat.tamtam.bot.domain.builder.validator.Validator;

@Repository
public interface ComponentValidatorRepository extends CrudRepository<Validator, Long> {
    Iterable<Validator> findAllByComponentId(Long componentId);
}
