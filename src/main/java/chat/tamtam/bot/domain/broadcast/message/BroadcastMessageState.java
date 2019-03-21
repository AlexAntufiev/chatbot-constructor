package chat.tamtam.bot.domain.broadcast.message;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum BroadcastMessageState {
    SCHEDULED((byte) 0),
    SENT((byte) 1),
    ERROR((byte) 2),
    DELETED((byte) 3);

    @Getter
    private final byte value;
}
