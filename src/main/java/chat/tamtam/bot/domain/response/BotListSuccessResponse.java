package chat.tamtam.bot.domain.response;

import java.util.List;

import chat.tamtam.bot.domain.BotSchemeEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class BotListSuccessResponse extends SuccessResponse {
    private List<BotSchemeEntity> botList;

    public BotListSuccessResponse(List<BotSchemeEntity> botList) {
        this.botList = botList;
    }
}
