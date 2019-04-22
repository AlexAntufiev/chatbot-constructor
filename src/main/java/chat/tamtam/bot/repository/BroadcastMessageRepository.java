package chat.tamtam.bot.repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import chat.tamtam.bot.domain.broadcast.message.BroadcastMessageEntity;

@Repository
public interface BroadcastMessageRepository extends CrudRepository<BroadcastMessageEntity, Long> {
    BroadcastMessageEntity findByBotSchemeIdAndTamBotIdAndChatChannelIdAndIdAndStateIsNotIn(
            int botSchemeId,
            long tamBotId,
            long chatChannelId,
            long messageId,
            Collection<Byte> excludedStates
    );

    List<BroadcastMessageEntity> findAllByBotSchemeIdAndTamBotIdAndChatChannelIdAndStateIsNotIn(
            int botSchemeId,
            long tamBotId,
            long chatChannelId,
            Collection<Byte> excludedStates
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

    List<BroadcastMessageEntity> findAllByFiringTimeBeforeAndState(Instant instant, byte state);

    List<BroadcastMessageEntity> findAllByErasingTimeBeforeAndState(Instant instant, byte state);
}
