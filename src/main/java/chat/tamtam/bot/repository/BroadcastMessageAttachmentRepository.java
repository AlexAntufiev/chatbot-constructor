package chat.tamtam.bot.repository;

import org.springframework.data.repository.CrudRepository;

import chat.tamtam.bot.domain.broadcast.message.attachment.BroadcastMessageAttachment;

public interface BroadcastMessageAttachmentRepository extends CrudRepository<BroadcastMessageAttachment, Long> {

}
