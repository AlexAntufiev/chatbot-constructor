package chat.tamtam.bot.domain.response;

import java.util.List;

import chat.tamtam.bot.domain.broadcast.message.BroadcastMessageEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class BroadcastMessageListSuccessReponse extends SuccessResponse {
    @Getter
    private final List<BroadcastMessageEntity> broadcastMessages;
}
