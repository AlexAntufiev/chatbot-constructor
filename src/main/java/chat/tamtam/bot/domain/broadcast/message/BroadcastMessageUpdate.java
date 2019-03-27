package chat.tamtam.bot.domain.broadcast.message;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BroadcastMessageUpdate {
    private String title;
    private String text;
    private String firingTime;
    private String erasingTime;
    // @todo #CC-63 Add payload field
}
