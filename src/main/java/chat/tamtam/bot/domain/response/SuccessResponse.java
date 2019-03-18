package chat.tamtam.bot.domain.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class SuccessResponse {
    private final Boolean success = true;
}
