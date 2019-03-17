package chat.tamtam.bot.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import chat.tamtam.bot.domain.channel.TamChannelEntity;

@Repository
public interface TamChannelRepository extends CrudRepository<TamChannelEntity, Long> {
    Iterable<TamChannelEntity> findAllByChannelId_BotSchemeIdAndChannelId_TamBotId(
            int botSchemeId,
            long tamBotId
    );

    TamChannelEntity findByChannelId_BotSchemeIdAndChannelId_TamBotIdAndChannelId_ChatId(
            int botSchemeId,
            long tamBotId,
            long chatId
    );
}
