package chat.tamtam.bot.domain.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class SuccessResponse {
    @Getter
    private final Boolean success = true;
}
