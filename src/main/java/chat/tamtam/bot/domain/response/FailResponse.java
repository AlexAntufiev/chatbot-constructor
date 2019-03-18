package chat.tamtam.bot.domain.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class FailResponse {
    private final Boolean success = false;
    private String error;

    public FailResponse(String error) {
        this.error = error;
    }
}
