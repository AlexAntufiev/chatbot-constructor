package chat.tamtam.bot.domain.webhook;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Message {
    private String mid;
    private String seq;
    private String text;
    private List<Attachment> attachments;
}
