package chat.tamtam.bot.domain.response;

import chat.tamtam.bot.domain.BotSchemaInfoEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class BotSubscriptionSuccessEntity {
    private final Boolean success = true;
    private BotSchemaInfoEntity info;

    public BotSubscriptionSuccessEntity(BotSchemaInfoEntity info) {
        this.info = info;
    }
}
