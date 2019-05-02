package chat.tamtam.bot.domain.bot.hockey;

import java.net.URL;
import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import chat.tamtam.botapi.model.NewMessageBody;
import lombok.Data;

@Data
public class Match {

    private int id;
    @JsonProperty("team1_id")
    private String team1Id;
    @JsonProperty("team2_id")
    private String team2Id;
    private Instant date;
    private String score;
    private String state;
    @JsonProperty("is_active")
    private int active;
    private URL url;

    private List<Event> events;

    public List<NewMessageBody> getMessages() {
        return null;
    }

    private static class Event {

        private String period;
        private String text;
        private String time;
        private String type;
        private String team;

    }
}
