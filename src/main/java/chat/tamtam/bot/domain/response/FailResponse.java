package chat.tamtam.bot.domain.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class FailResponse {
    @Getter
    private final Boolean success = false;
    @Getter
    private String error;

    public FailResponse(String error) {
        this.error = error;
    }
}
