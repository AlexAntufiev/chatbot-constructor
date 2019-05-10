package chat.tamtam.bot.domain.bot.hockey;

import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonProperty;

import chat.tamtam.bot.utils.DateUtils;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@NoArgsConstructor
public class Match {

    private int id;
    private String type;
    @JsonProperty("team1_id")
    private String team1Id;
    private String score;
    private String state;
    @JsonProperty("is_active")
    private int active;
    private Instant date;
    @JsonProperty("team2_id")
    private String team2Id;
    private List<Event> events;

    public String getMatchInfo() {
        Team team1 = Team.getById(Integer.parseInt(team1Id));
        Team team2 = Team.getById(Integer.parseInt(team2Id));
        return String.format("%s\n%s %s %s\n%s",
                DateUtils.instantToString(date),
                team1.getName(),
                score,
                team2.getName(),
                state
        );
    }

    public Stream<String> getMessages() {
        return events.stream().map(Event::getInfo);
    }

    @Setter
    @NoArgsConstructor
    private static class Event {

        private String period;
        private String text;
        private String time;
        private Type type;
        private int id;
        private String team;

        @AllArgsConstructor
        private enum Type {
            @JsonProperty("status") STATUS,
            @JsonProperty("in") IN,
            @JsonProperty("penalty") PENALTY,
            @JsonProperty("goal") GOAL,
            ;

        }

        private String getInfo() {
            switch (type) {
                case STATUS:
                    return text;
                case IN:
                case GOAL:
                case PENALTY:
                    if ("0".equals(period)) {
                        return String.format("%s: %s", team, text);
                    } else {
                        return String.format("%s период %s\n%s: %s", period, time, team, text);
                    }
                default:
                    throw new IllegalStateException(String.format("Illegal type %s", type));
            }
        }

    }
}
