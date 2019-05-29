package chat.tamtam.bot.domain.vote;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Attachment {
    private String type;
    private String url;
}
