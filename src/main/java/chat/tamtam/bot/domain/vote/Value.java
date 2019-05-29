package chat.tamtam.bot.domain.vote;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Value {
    private String text;
    private List<Attachment> attachments;
}
