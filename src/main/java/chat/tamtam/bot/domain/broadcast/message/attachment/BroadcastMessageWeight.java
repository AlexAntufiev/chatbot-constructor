package chat.tamtam.bot.domain.broadcast.message.attachment;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum BroadcastMessageWeight {
    PHOTO((byte) 1),
    VIDEO((byte) 1),
    AUDIO((byte) 10),
    FILE((byte) 10),
    ;

    @Getter
    private final byte weight;

    private static final byte MAX_MESSAGE_WEIGHT = 10;

    public static byte getWeight(final BroadcastMessageAttachment attachment) {
        return valueOf(attachment.getUploadType().name()).weight;
    }

    public static boolean isWeightExceedsMax(final byte weight) {
        return weight > MAX_MESSAGE_WEIGHT;
    }
}
