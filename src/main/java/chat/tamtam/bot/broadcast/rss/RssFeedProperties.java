package chat.tamtam.bot.broadcast.rss;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Component
@RefreshScope
@NoArgsConstructor
@ConfigurationProperties(prefix = "tamtam.rss")
public class RssFeedProperties {
    private Bot bot;
    private List<Feed> feeds = new ArrayList<>();

    @Data
    @NoArgsConstructor
    public static class Feed {
        private Boolean enabled;
        private Long channel;
        private String url;
        private String format;
    }

    @Data
    @NoArgsConstructor
    public static class Bot {
        private String token;
    }
}
