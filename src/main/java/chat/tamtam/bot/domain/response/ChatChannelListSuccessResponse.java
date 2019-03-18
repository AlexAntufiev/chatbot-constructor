package chat.tamtam.bot.domain.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class ChatChannelListSuccessResponse<T> extends SuccessResponse {
    private Iterable<T> chatChannels;
    private Long marker;

    public ChatChannelListSuccessResponse(final Iterable<T> chatChannels, final Long marker) {
        this.chatChannels = chatChannels;
        this.marker = marker;
    }
}
