package chat.tamtam.bot.domain.broadcast.message;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum BroadcastMessageState {
    SCHEDULED((byte) 0),
    SENT((byte) 1),
    ERASED((byte) 2),
    ERROR((byte) 3),
    DELETED((byte) 4),
    PROCESSING((byte) 5);

    @Getter
    private final byte value;
}
