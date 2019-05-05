package chat.tamtam.bot.repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import chat.tamtam.bot.domain.broadcast.message.BroadcastMessage;

@Repository
public interface BroadcastMessageRepository extends CrudRepository<BroadcastMessage, Long> {
    BroadcastMessage findByBotSchemeIdAndTamBotIdAndChatChannelIdAndIdAndStateIsNotIn(
            int botSchemeId,
            long tamBotId,
            long chatChannelId,
            long messageId,
            Collection<Byte> excludedStates
    );

    List<BroadcastMessage> findAllByBotSchemeIdAndTamBotIdAndChatChannelIdAndStateIsNotIn(
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

    List<BroadcastMessage> findAllByFiringTimeBeforeAndState(Instant instant, byte state);

    List<BroadcastMessage> findAllByErasingTimeBeforeAndState(Instant instant, byte state);
}
