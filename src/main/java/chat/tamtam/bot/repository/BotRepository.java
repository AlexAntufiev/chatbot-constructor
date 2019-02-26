package chat.tamtam.bot.repository;

import chat.tamtam.bot.domain.Bot;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BotRepository extends CrudRepository<Bot, Integer> { }
