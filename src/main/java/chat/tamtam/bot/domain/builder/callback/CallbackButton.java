package chat.tamtam.bot.domain.builder.callback;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CallbackButton {
    private String payload;
    private String text;
    private String intent;
}
