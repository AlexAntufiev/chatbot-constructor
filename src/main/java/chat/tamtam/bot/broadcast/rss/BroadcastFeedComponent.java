package chat.tamtam.bot.broadcast.rss;

import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.annotation.PostConstruct;

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

import chat.tamtam.bot.domain.broadcast.rss.RssFeedEntry;
import chat.tamtam.bot.repository.RssFeedRepository;
import chat.tamtam.bot.utils.TransactionalUtils;
import chat.tamtam.botapi.TamTamBotAPI;
import chat.tamtam.botapi.exceptions.APIException;
import chat.tamtam.botapi.exceptions.ClientException;
import chat.tamtam.botapi.model.Chat;
import chat.tamtam.botapi.model.ChatMember;
import chat.tamtam.botapi.model.ChatType;
import chat.tamtam.botapi.model.NewMessageBody;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
@RefreshScope
@ConditionalOnProperty(
        prefix = "tamtam.rss",
        name = "enabled",
        havingValue = "true"
)
@RequiredArgsConstructor
public class BroadcastFeedComponent {
    private static final long DEFAULT_REFRESH_RATE = 10_000L;

    private final ThreadPoolTaskExecutor broadcastFeedExecutor;
    private final RssFeedProperties properties;
    private final RssFeedRepository rssFeedRepository;

    private final TransactionalUtils transactionalUtils;

    private TamTamBotAPI api;

    @PostConstruct
    public void init() {
        log.info(
                String.format(
                        "BroadcastRssFeedComponent(bot=%s, feeds=%s) initializing...",
                        properties.getBot(),
                        properties.getFeeds()
                )
        );

        api = TamTamBotAPI.create(properties.getBot().getToken());

        properties
                .getFeeds()
                .stream()
                .filter(this::isWritableChannel)
                .forEach(entry -> {
                    transactionalUtils.invokeRunnable(() -> {
                        if (entry.getEnabled()) {
                            enableFeed(entry);
                        } else {
                            disableFeed(entry);
                        }
                    });
                });
    }

    private void disableFeed(final RssFeedProperties.Feed feed) {
        rssFeedRepository
                .findByFeedIdChannelIdAndFeedIdUrlAndEnabled(feed.getChannel(), feed.getUrl(), true)
                .ifPresent(entry -> {
                    entry.setEnabled(false);
                    rssFeedRepository.save(entry);
                });
        log.info(String.format("Feed(%s) was disabled by configuration", feed));
    }

    private void enableFeed(final RssFeedProperties.Feed feed) {
        rssFeedRepository
                .findByFeedIdChannelIdAndFeedIdUrl(feed.getChannel(), feed.getUrl())
                .ifPresentOrElse(entry -> {
                    if (!entry.getEnabled()) {
                        entry.setEnabled(true);
                        rssFeedRepository.save(entry);
                    }
                }, () -> {
                    rssFeedRepository.save(
                            new RssFeedEntry(
                                    new RssFeedEntry.FeedId(feed.getChannel(), feed.getUrl()),
                                    Instant.now(),
                                    true,
                                    feed.getFormat()
                            )
                    );
                });
        log.info(String.format("Feed(%s) was enabled by configuration", feed));
    }

    private boolean isWritableChannel(final RssFeedProperties.Feed feed) {
        try {
            Chat chat = api.getChat(feed.getChannel()).execute();
            ChatMember member = api.getMembership(feed.getChannel()).execute();
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
        rssFeedRepository
                .findAllByEnabled(true)
                .forEach(feed -> futureList.add(
                        broadcastFeedExecutor.submit(() -> {
                            try {
                                SyndFeed externalFeed =
                                        new SyndFeedInput()
                                                .build(new XmlReader(new URL(feed.getFeedId().getUrl())));
                                updateFeed(externalFeed, feed);
                                rssFeedRepository.save(feed);
                            } catch (IOException | FeedException e) {
                                log.error(String.format("Can't fetch rss feed(%s)", feed), e);
                            }
                        })));
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

    private void updateFeed(final SyndFeed externalFeed, final RssFeedEntry feed) {
        externalFeed
                .getEntries()
                .stream()
                .filter(entry -> feed.getInstant().isBefore(entry.getPublishedDate().toInstant()))
                .sorted(((o1, o2) -> {
                    Instant i1 = o1.getPublishedDate().toInstant();
                    Instant i2 = o2.getPublishedDate().toInstant();
                    return i1.isBefore(i2) ? -1 : i1.equals(i2) ? 0 : 1;
                }))
                .forEach(entry -> feed.setInstant(sendMessage(entry, feed)));
    }

    private Instant sendMessage(final SyndEntry entry, final RssFeedEntry feed) {
        try {
            api.sendMessage(getMessageBody(entry, feed))
                    .chatId(feed.getFeedId().getChannelId())
                    .execute();
            return entry.getPublishedDate().toInstant();
        } catch (APIException | ClientException | IllegalStateException e) {
            log.error(String.format("Can't send message(feed=%s, api=%s)", feed, api), e);
            return feed.getInstant();
        }
    }

    private NewMessageBody getMessageBody(final SyndEntry entry, final RssFeedEntry feed) {
        switch (feed.getFormat()) {
            case FeedFormat.TITLE_AND_LINK:
                return new NewMessageBody(
                        new StringBuilder()
                                .append(entry.getTitle())
                                .append("\n")
                                .append(entry.getLink())
                                .toString(),
                        null
                );
            default:
                throw new IllegalStateException(String.format("Unknown feed format(%s)", feed));
        }
    }
}
