package chat.tamtam.bot.domain.response;

import chat.tamtam.bot.domain.TamBotEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class BotSubscriptionSuccessEntity {
    private final Boolean success = true;
    private TamBotEntity info;

    public BotSubscriptionSuccessEntity(TamBotEntity info) {
        this.info = info;
    }
}
