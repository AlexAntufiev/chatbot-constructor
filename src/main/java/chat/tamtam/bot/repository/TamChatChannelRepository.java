package chat.tamtam.bot.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import chat.tamtam.bot.domain.chat.TamChatEntity;

@Repository
public interface TamChatChannelRepository extends CrudRepository<TamChatEntity, Long> {
    Iterable<TamChatEntity> findAllByIdBotSchemeIdAndIdTamBotId(
            int botSchemeId,
            long tamBotId
    );

    TamChatEntity findByIdBotSchemeIdAndIdTamBotIdAndIdChatId(
            int botSchemeId,
            long tamBotId,
            long chatId
    );
}
