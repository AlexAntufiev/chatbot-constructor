package chat.tamtam.bot.domain.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class ChatChannelFailResponse extends FailResponse {
    private Long chatChannel;

    public ChatChannelFailResponse(final String errorKey, final Long chatChannel) {
        super(errorKey);
        this.chatChannel = chatChannel;
    }
}
