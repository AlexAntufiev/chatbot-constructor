package chat.tamtam.bot.broadcast.rss;

import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import chat.tamtam.bot.domain.broadcast.rss.RssFeed;
import chat.tamtam.bot.repository.RssFeedRepository;
import chat.tamtam.bot.utils.TransactionalUtils;
import chat.tamtam.botapi.TamTamBotAPI;
import chat.tamtam.botapi.exceptions.APIException;
import chat.tamtam.botapi.exceptions.ClientException;
import chat.tamtam.botapi.model.Chat;
import chat.tamtam.botapi.model.ChatMember;
import chat.tamtam.botapi.model.ChatType;
import chat.tamtam.botapi.model.NewMessageBody;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
@RefreshScope
@ConditionalOnProperty(
        prefix = "tamtam.rss",
        name = "enabled",
        havingValue = "true"
)
public class BroadcastFeedComponent {
    private static final long DEFAULT_REFRESH_RATE = 10_000L;

    @Value("${tamtam.rss.enabled}")
    private Boolean enabled;

    private final ThreadPoolTaskExecutor broadcastFeedExecutor;

    private final RssFeedRepository rssFeedRepository;

    private final TransactionalUtils transactionalUtils;

    private final TamTamBotAPI api;

    public BroadcastFeedComponent(
            @Value("${tamtam.rss.token}") final String token,
            final ThreadPoolTaskExecutor broadcastFeedExecutor,
            final RssFeedRepository rssFeedRepository,
            final TransactionalUtils transactionalUtils
    ) {
        log.info(
                String.format(
                        "BroadcastRssFeedComponent(token=%s, core pool size=%d, max pool size=%d) initializing...",
                        token, broadcastFeedExecutor.getCorePoolSize(), broadcastFeedExecutor.getMaxPoolSize()
                )
        );
        this.broadcastFeedExecutor = broadcastFeedExecutor;
        this.rssFeedRepository = rssFeedRepository;
        this.transactionalUtils = transactionalUtils;
        api = TamTamBotAPI.create(token);
        transactionalUtils.invokeRunnable(this::init);
    }

    private void init() {
        StreamSupport.stream(rssFeedRepository.findAllByEnabled(true).spliterator(), false)
                .forEach(feed -> {
                    if (isWritableChannel(feed)) {
                        log.info(String.format("RSS %s - enabled", feed));
                    } else {
                        log.warn(String.format("RSS %s has insufficient permissions - disabled", feed, api));
                    }
                });
    }

    private boolean isWritableChannel(final RssFeed feed) {
        try {
            Chat chat = api.getChat(feed.getFeedId().getChannelId()).execute();
            ChatMember member = api.getMembership(feed.getFeedId().getChannelId()).execute();
            if (chat.getType() != ChatType.CHANNEL) {
                throw new IllegalStateException("Chat is not channel");
            }
            if (member.getPermissions() == null) {
                throw new IllegalStateException("Insufficient permissions in channel");
            }
            return true;
        } catch (APIException | ClientException | IllegalStateException e) {
            log.error(String.format("Error during feed(%s) channel check", feed), e);
            return false;
        }
    }

    @Scheduled(fixedRate = DEFAULT_REFRESH_RATE)
    public void refresh() {
        List<Future> futureList = new ArrayList<>();
        StreamSupport.stream(rssFeedRepository.findAllByEnabled(true).spliterator(), false)
                .filter(this::isWritableChannel)
                .forEach(
                        feed -> futureList.add(broadcastFeedExecutor.submit(() -> submit(feed)))
                );

        log.info(String.format("RSS: refreshing %d feeds on executor %s", futureList.size(), broadcastFeedExecutor));

        // @todo #CC-212 Change execution flow - let's store tasks in database with markers
        futureList.forEach(this::get);
    }

    private void get(Future<?> future) {
        try {
            future.get();
        } catch (ExecutionException | InterruptedException e) {
            log.error("Waiting for feeds update produce exception", e);
        }
    }

    private void submit(final RssFeed feed) {
        try {
            if (feed.getPostsPerUpdate() < 0) {
                throw new IllegalStateException(
                        String.format("Parameter postsPerUpdate=%d(less 0)", feed.getPostsPerUpdate())
                );
            }
            if (feed.getRefreshPeriod() < 0) {
                throw new IllegalStateException(
                        String.format("Parameter refreshPeriod=%d(less 0)", feed.getRefreshPeriod())
                );
            }
            SyndFeed externalFeed =
                    new SyndFeedInput()
                            .build(new XmlReader(new URL(feed.getFeedId().getUrl())));

            Instant now = Instant.now();

            // In case of first feed refresh
            if (feed.getLastUpdate() == null) {
                feed.setLastUpdate(now);
                feed.setLastRefresh(now);
                updateAndStore(externalFeed, feed);
                return;
            }

            // In case if lastRefresh was set to null
            if (feed.getLastRefresh() == null) {
                feed.setLastRefresh(now);
                updateAndStore(externalFeed, feed);
                return;
            }

            // In case if it is time to refresh and update
            if (now.getEpochSecond() - feed.getLastRefresh().getEpochSecond() >= feed.getRefreshPeriod()) {
                feed.setLastRefresh(now);
                updateAndStore(externalFeed, feed);
            }

        } catch (IOException | FeedException e) {
            log.error(String.format("Can't fetch rss feed(%s)", feed), e);
        }
    }

    private void updateAndStore(final SyndFeed externalFeed, final RssFeed feed) {
        updateFeed(externalFeed, feed);
        rssFeedRepository.save(feed);
    }

    private void updateFeed(final SyndFeed externalFeed, final RssFeed feed) {
        List<SyndEntry> updates =
                externalFeed
                        .getEntries()
                        .stream()
                        .filter(entry -> feed.getLastUpdate().isBefore(entry.getPublishedDate().toInstant()))
                        .sorted((o1, o2) -> {
                            Instant i1 = o1.getPublishedDate().toInstant();
                            Instant i2 = o2.getPublishedDate().toInstant();
                            return i1.isBefore(i2) ? -1 : i1.equals(i2) ? 0 : 1;
                        })
                        .collect(Collectors.toList());
        if (updates.isEmpty()) {
            return;
        }
        sendUpdates(feed, updates);
    }

    private void sendUpdates(final RssFeed feed, List<SyndEntry> updates) {
        if (feed.getPostsPerUpdate() == 0 || feed.getPostsPerUpdate() >= updates.size()) {
            sendAll(feed, updates);
            return;
        }

        short amount = feed.getPostsPerUpdate();
        int startsFrom = updates.size() - amount;

        for (int i = startsFrom; i < updates.size(); i++) {
            sendMessage(updates.get(i), feed);
        }

        log.info(String.format("BroadcastFeedComponent sent %d messages to %s", updates.size() - startsFrom, feed));
    }

    private void sendAll(final RssFeed feed, List<SyndEntry> updates) {
        updates.forEach(entry -> sendMessage(entry, feed));
        log.info(String.format("BroadcastFeedComponent sent %d messages to %s", updates.size(), feed));
    }

    private void sendMessage(final SyndEntry entry, final RssFeed feed) {
        try {
            api.sendMessage(getMessageBody(entry, feed))
                    .chatId(feed.getFeedId().getChannelId())
                    .execute();
            feed.setLastUpdate(entry.getPublishedDate().toInstant());
        } catch (APIException | ClientException | IllegalStateException e) {
            log.error(String.format("Can't send message(feed=%s, api=%s)", feed, api), e);
        }
    }

    private NewMessageBody getMessageBody(final SyndEntry entry, final RssFeed feed) {
        switch (feed.getFormat()) {
            case FeedFormat.TITLE_LINK:
                return new NewMessageBody(
                        new StringBuilder()
                                .append(entry.getTitle())
                                .append("\n")
                                .append(entry.getLink())
                                .toString(),
                        null
                );
            case FeedFormat.TITLE_LINK_TIME:
                return new NewMessageBody(
                        new StringBuilder()
                                .append(entry.getTitle())
                                .append("\n")
                                .append(entry.getLink())
                                .append("\n")
                                .append(entry.getPublishedDate())
                                .toString(),
                        null
                );
            default:
                throw new IllegalStateException(String.format("Unknown feed format(%s)", feed));
        }
    }
}
