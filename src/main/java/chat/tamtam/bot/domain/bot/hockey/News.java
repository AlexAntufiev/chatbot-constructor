package chat.tamtam.bot.domain.bot.hockey;

import java.net.URL;
import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class News {

    private List<Entity> news;

    @Data
    private static class Entity {

        private String subtitle;
        private Instant date;
        private URL url;
        @JsonProperty("img_url")
        private URL imageUrl;
        private String title;
        private int id;
    }
}
