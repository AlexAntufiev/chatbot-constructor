package chat.tamtam.bot.domain.response;

import java.util.List;

import chat.tamtam.botapi.model.Chat;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class ChatChannelListResponse {
    private List<Chat> chats;
    private Long marker;

    public ChatChannelListResponse(final List<Chat> chats, final Long marker) {
        this.chats = chats;
        this.marker = marker;
    }
}
