package chat.tamtam.bot.domain.broadcast.message;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NewBroadcastMessage {
    private String title;
    private Long firingTime;
    private Long erasingTime;
    private String text;
    // @todo #CC-63 Add payload field
}
