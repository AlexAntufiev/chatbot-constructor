package chat.tamtam.bot.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import chat.tamtam.bot.domain.chatchannel.ChatChannelEntity;

@Repository
public interface ChatChannelRepository extends CrudRepository<ChatChannelEntity, Long> {
    Iterable<ChatChannelEntity> findAllByIdBotSchemeIdAndIdTamBotId(
            int botSchemeId,
            long tamBotId
    );

    ChatChannelEntity findByIdBotSchemeIdAndIdTamBotIdAndIdChatId(
            int botSchemeId,
            long tamBotId,
            long chatId
    );
}