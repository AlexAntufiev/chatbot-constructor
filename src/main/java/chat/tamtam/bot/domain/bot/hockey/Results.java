package chat.tamtam.bot.domain.bot.hockey;

import java.net.URL;
import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class Results {

    private List<Entity> matches;

    @Data
    private static class Entity {

        private int id;
        private String team1;
        private String team2;
        private Instant date;
        private String score;
        private String state;
        @JsonProperty("is_active")
        private int active;
        private URL url;
    }
}
