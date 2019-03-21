package chat.tamtam.bot.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import chat.tamtam.bot.domain.broadcast.message.BroadcastMessageEntity;

@Repository
public interface BroadcastMessageRepository extends CrudRepository<BroadcastMessageEntity, Long> {
    BroadcastMessageEntity findByBotSchemeIdAndTamBotIdAndChatChannelIdAndId(
            int botSchemeId,
            long tamBotId,
            long chatChannelId,
            long messageId
    );

    List<BroadcastMessageEntity> findAllByBotSchemeIdAndTamBotIdAndChatChannelId(
            int botSchemeId,
            long tamBotId,
            long chatChannelId
    );

    boolean existsByBotSchemeIdAndTamBotIdAndChatChannelIdAndId(
            int botSchemeId,
            long tamBotId,
            long chatChannelId,
            long messageId
    );

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    void deleteByBotSchemeIdAndTamBotIdAndChatChannelIdAndId(
            int botSchemeId,
            long tamBotId,
            long chatChannelId,
            long messageId
    );
}
