package chat.tamtam.bot.domain.broadcast.message;

import java.sql.Timestamp;
import java.util.TimeZone;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NewBroadcastMessage {
    private String title;
    private Timestamp firingTime;
    private TimeZone timeZone;
    private String text;
}
