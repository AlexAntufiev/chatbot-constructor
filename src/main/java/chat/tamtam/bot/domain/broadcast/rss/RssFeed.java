package chat.tamtam.bot.domain.broadcast.rss;

import java.io.Serializable;
import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class RssFeed {
    @EmbeddedId
    private FeedId feedId;
    /*
     * Presents last publish time.
     * */
    private Instant lastUpdate;
    private String format;
    private Boolean enabled;
    /*
     * Presents last refresh time(refresh event depends on refreshPeriod param).
     * */
    private Instant lastRefresh;
    /*
     * Defines time period IN SECONDS, after which we should send new posts in count of postsPerUpdate.
     * null -> each refresh
     * 0 -> each refresh
     * */
    @Column(nullable = false)
    private Long refreshPeriod;
    /*
     * Defines new posts amount(starts from newest), that will be published after each rss refresh.
     * null -> all available posts
     * 0 -> all available posts
     * */
    @Column(nullable = false)
    private Short postsPerUpdate;

    @Data
    @Embeddable
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeedId implements Serializable {
        private Long channelId;
        private String url;
    }
}
