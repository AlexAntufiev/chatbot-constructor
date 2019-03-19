package chat.tamtam.bot.domain.broadcast.message;

import lombok.Getter;

public enum BroadcastMessageState {
    SCHEDULED((byte) 0),
    SENT((byte) 1),
    ERROR((byte) 2),
    ;

    @Getter
    private final byte value;

    BroadcastMessageState(byte v) {
        value = v;
    }
}
