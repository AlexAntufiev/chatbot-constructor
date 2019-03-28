package chat.tamtam.bot.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import chat.tamtam.bot.domain.broadcast.message.attachment.BroadcastMessageAttachment;

@Repository
public interface BroadcastMessageAttachmentRepository extends CrudRepository<BroadcastMessageAttachment, Long> {
    Iterable<BroadcastMessageAttachment> findAllByBroadcastMessageId(long messageId);
}
