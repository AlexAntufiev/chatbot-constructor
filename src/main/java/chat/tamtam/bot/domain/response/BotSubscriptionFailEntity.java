package chat.tamtam.bot.domain.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class BotSubscriptionFailEntity {
    private final Boolean success = false;
    private String error;

    public BotSubscriptionFailEntity(String error) {
        this.error = error;
    }
}
