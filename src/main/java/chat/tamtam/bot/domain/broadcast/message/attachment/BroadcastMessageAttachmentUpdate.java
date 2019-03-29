package chat.tamtam.bot.domain.broadcast.message.attachment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BroadcastMessageAttachmentUpdate {
    private String type;
    private String token;
    private String title;
}
