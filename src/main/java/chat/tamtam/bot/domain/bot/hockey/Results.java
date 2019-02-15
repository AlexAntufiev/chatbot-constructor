package chat.tamtam.bot.domain.bot.hockey;

import java.net.URL;
import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import chat.tamtam.bot.utils.DateUtils;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class Results {

    @Setter
    private List<Entity> matches;

    public String getMessages() {
        if (matches.isEmpty()) {
            return "Матчи еще не начались. Наберись терпения";
        } else {
            StringBuilder stringBuilder = new StringBuilder();

            matches.forEach(entity -> {
                stringBuilder.append(entity.getInfo());
                stringBuilder.append("\n\n");
            });
            return stringBuilder.toString();
        }
    }

    @Setter
    @NoArgsConstructor
    private static class Entity {

        private int id;
        private String team1;
        private String team2;
        private Instant date;
        private String score;
        private String state;
        private String stage;
        @JsonProperty("is_active")
        private int active;
        private URL url;

        public String getInfo() {
            return String.format(
                    "%s\n%s\n%s\n%s %s %s\n%s",
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
