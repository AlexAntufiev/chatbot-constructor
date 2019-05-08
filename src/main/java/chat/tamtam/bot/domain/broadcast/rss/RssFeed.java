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
    private Instant instant;
    private String format;
    private Boolean enabled;
    /*
     * Defines time period IN SECONDS, after which we should send new posts in count of postsPerUpdate.
     * null -> each refresh
     * 0 -> each refresh
     * */
    @Column(nullable = false)
    private Long updatePeriod;
    /*
     * Defines new posts amount(starts from newest), that will be published after each rss refresh.
     * null -> all available posts
     * 0 -> all available posts
     * */
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
