package chat.tamtam.bot.domain.response;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class TamBotChatsListResponse {
    private List<TamChatResponse> chats;
    private Long marker;

    public TamBotChatsListResponse(final List<TamChatResponse> chats, final Long marker) {
        this.chats = chats;
        this.marker = marker;
    }
}
