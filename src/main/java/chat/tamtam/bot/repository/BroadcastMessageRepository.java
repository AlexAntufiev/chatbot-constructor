package chat.tamtam.bot.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import chat.tamtam.bot.domain.broadcast.message.MessageEntity;

@Repository
public interface BroadcastMessageRepository extends CrudRepository<MessageEntity, Long> {

}
