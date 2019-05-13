package chat.tamtam.bot.domain.bot.hockey;

import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonProperty;

import chat.tamtam.bot.utils.DateUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
public class Calendar {

    private List<Entity> matches;

    public String getMessages() {
        StringBuilder stringBuilder = new StringBuilder();

        matches.forEach(entity -> {
            stringBuilder.append(entity.getInfo());
            stringBuilder.append("\n\n");
        });
        return stringBuilder.toString();
    }

    public Stream<Entity> getAvailableMatches() {
        return matches.stream()
                .filter(entity -> entity.active == 1);
    }

    @Setter
    @NoArgsConstructor
    public static class Entity {

        @Getter
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

        public String getMatchInfo() {
            return String.format(
                    "%s - %s",
                    Team.getById(Integer.parseInt(team1Id)).getName(),
                    Team.getById(Integer.parseInt(team2Id)).getName()
            );
        }

        public String getInfo() {
            return String.format(
                    "%s\n%s\n%s\n%s\n%s %s %s\n%s",
                    location,
                    DateUtils.instantToString(date),
                    stage,
                    state,
                    team1,
                    score,
                    team2,
                    url
            );
        }
    }
}
