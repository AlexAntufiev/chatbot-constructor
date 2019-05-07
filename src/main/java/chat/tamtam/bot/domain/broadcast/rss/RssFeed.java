package chat.tamtam.bot.domain.broadcast.rss;

import java.io.Serializable;
import java.time.Instant;

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

    @Data
    @Embeddable
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeedId implements Serializable {
        private Long channelId;
        private String url;
    }
}
