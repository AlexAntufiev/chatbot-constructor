package chat.tamtam.bot.domain.bot.hockey;

import java.net.URL;
import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class Calendar {

    private List<Entity> matches;

    @Data
    private static class Entity {

        private int id;
        private String team1;
        @JsonProperty("team1_id")
        private String team1Id;
        private String team2;
        @JsonProperty("team2_id")
        private String team2Id;
        private String stage;
        private String location;
        private String tv;
        private Instant date;
        private String score;
        private String state;
        @JsonProperty("is_active")
        private int active;
        private URL url;
    }
}
