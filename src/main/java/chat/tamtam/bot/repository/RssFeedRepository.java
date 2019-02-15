package chat.tamtam.bot.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import chat.tamtam.bot.domain.broadcast.rss.RssFeed;

@Repository
public interface RssFeedRepository extends CrudRepository<RssFeed, Long> {
    Iterable<RssFeed> findAllByEnabled(Boolean enabled);
}
