package chat.tamtam.bot.domain.bot.hockey;

import java.net.URL;
import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Getter(AccessLevel.NONE)
@Data
public class News {

    private List<Entity> news;

    @Data
    private static class Entity {

        private String subtitle;
        private Instant date;
        private URL url;
        @JsonProperty("img_url")
        private String imageUrl;
        private String title;
        private int id;

        private String getInfo() {
            return title + "\n" + subtitle + "\n\n" + "Читать подробнее: " + url;
        }
    }

    public String getMessages() {
        StringBuilder stringBuilder = new StringBuilder();

        news.forEach(entity -> {
            stringBuilder.append(entity.getInfo());
            stringBuilder.append("\n\n");
        });
        return stringBuilder.toString();
    }

}
