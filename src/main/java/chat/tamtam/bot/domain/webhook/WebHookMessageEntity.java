package chat.tamtam.bot.domain.webhook;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WebHookMessageEntity {
    private Sender sender;
    private Recipient recipient;
    private Long timestamp;
    private Message message;
}
