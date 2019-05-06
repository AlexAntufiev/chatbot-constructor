package chat.tamtam.bot.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import chat.tamtam.bot.domain.broadcast.rss.RssFeedEntry;

@Repository
public interface RssFeedRepository extends CrudRepository<RssFeedEntry, Long> {
    Optional<RssFeedEntry> findByFeedIdChannelIdAndFeedIdUrl(Long channelId, String url);

    Optional<RssFeedEntry> findByFeedIdChannelIdAndFeedIdUrlAndEnabled(Long channelId, String url, Boolean enabled);

    Iterable<RssFeedEntry> findAllByEnabled(Boolean enabled);
}
