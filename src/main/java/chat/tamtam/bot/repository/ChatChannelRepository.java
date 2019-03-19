package chat.tamtam.bot.repository;


import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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

    boolean existsByIdBotSchemeIdAndIdTamBotIdAndIdChatId(
            int botSchemeId,
            long tamBotId,
            long chatId
    );

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    void removeByIdBotSchemeIdAndIdTamBotIdAndIdChatId(
            int botSchemeId,
            long tamBotId,
            long chatId
    );
}
