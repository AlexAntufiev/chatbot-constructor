package chat.tamtam.bot.domain.response;

import java.util.Collections;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class TamChatFailResponse extends FailResponse {
    private List<Long> chats = Collections.emptyList();

    public TamChatFailResponse(final String errorKey, final List<Long> payload) {
        super(errorKey);
        chats = payload;
    }
}
