package chat.tamtam.bot.domain.broadcast.message;

public enum BroadcastMessageState {
    SCHEDULED((byte) 0),
    SENT((byte) 1),
    ERROR((byte) 2),
    ;

    public final byte value;

    BroadcastMessageState(byte v) {
        value = v;
    }
}
