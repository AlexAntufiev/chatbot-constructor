package chat.tamtam.bot.domain.response;

import java.util.List;

import chat.tamtam.bot.domain.BotSchemeEntity;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class BotListSuccessResponse extends SuccessResponse {
    private List<BotSchemeEntity> botList;
}
