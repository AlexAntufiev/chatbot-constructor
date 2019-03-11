package chat.tamtam.bot.domain.webhook;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Attachment {
    private String type;
    private Payload payload;
}