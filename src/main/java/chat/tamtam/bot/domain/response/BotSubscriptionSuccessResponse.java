package chat.tamtam.bot.domain.response;

import chat.tamtam.bot.domain.bot.TamBotEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class BotSubscriptionSuccessResponse extends SuccessResponse {
    private TamBotEntity info;

    public BotSubscriptionSuccessResponse(TamBotEntity info) {
        this.info = info;
    }
}
