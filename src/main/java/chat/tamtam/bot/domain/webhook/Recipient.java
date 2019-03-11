package chat.tamtam.bot.domain.webhook;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Recipient {
    @JsonProperty("chat_id")
    private Long chatId;
    @JsonProperty("chat_type")
    private String chatType;
    @JsonProperty("user_id")
    private Long userId;
}
