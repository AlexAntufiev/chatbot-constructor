package chat.tamtam.bot.domain.broadcast.message.attachment;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum BroadcastMessageWeight {
    PHOTO((byte) 1),
    VIDEO((byte) 1),
    AUDIO((byte) 10),
    FILE((byte) 10),
    MAX_MESSAGE_WEIGHT((byte) 10),
    ;

    @Getter
    private final byte weight;
}
