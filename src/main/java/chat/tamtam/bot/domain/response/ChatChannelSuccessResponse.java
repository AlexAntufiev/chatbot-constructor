package chat.tamtam.bot.domain.response;

import chat.tamtam.bot.domain.chatchannel.ChatChannelEntity;
import lombok.Data;

@Data
public class ChatChannelSuccessResponse extends SuccessResponse {
    private final ChatChannelEntity chatChannel;

    public ChatChannelSuccessResponse(ChatChannelEntity chatChannel) {
        this.chatChannel = chatChannel;
    }
}
