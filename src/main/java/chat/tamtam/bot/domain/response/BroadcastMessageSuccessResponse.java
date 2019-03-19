package chat.tamtam.bot.domain.response;

import chat.tamtam.bot.domain.broadcast.message.BroadcastMessageEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class BroadcastMessageSuccessResponse extends SuccessResponse {
    @Getter
    private final BroadcastMessageEntity broadcastMessage;
}
