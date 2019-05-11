package chat.tamtam.bot.repository;

import org.springframework.data.repository.CrudRepository;

import chat.tamtam.bot.domain.builder.component.group.SchemeComponentGroup;

public interface ComponentGroupRepository extends CrudRepository<SchemeComponentGroup, Long> {

}
