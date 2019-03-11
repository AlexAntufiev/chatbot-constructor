package chat.tamtam.bot.domain.webhook;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Sender {
    @JsonProperty("user_id")
    private Long userId;
    private String name;
}
