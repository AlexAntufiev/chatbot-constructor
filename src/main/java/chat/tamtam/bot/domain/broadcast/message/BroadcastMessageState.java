package chat.tamtam.bot.domain.broadcast.message;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * SCHEDULED -> DISCARDED_BY_USER
 * SCHEDULED -> PROCESSING -> ERROR
 * SCHEDULED -> PROCESSING -> SENT
 * SENT -> PROCESSING -> ERASED_BY_SCHEDULE
 * SENT -> DISCARDED_ERASE_BY_USER
 * DISCARDED_SEND_BY_USER -> SCHEDULED
 */

@AllArgsConstructor
public enum BroadcastMessageState {
    SCHEDULED((byte) 0),
    SENT((byte) 1),
    ERASED_BY_SCHEDULE((byte) 2),
    ERROR((byte) 3),
    DISCARDED_SEND_BY_USER((byte) 4),
    DISCARDED_ERASE_BY_USER((byte) 5),
    PROCESSING((byte) 6),
    ;

    @Getter
    private final byte value;

    public static BroadcastMessageState getState(byte b) {
        switch (b) {
            case 0:
                return SCHEDULED;
            case 1:
                return SENT;
            case 2:
                return ERASED_BY_SCHEDULE;
            case 3:
                return ERROR;
            case 4:
                return DISCARDED_SEND_BY_USER;
            case 5:
                return DISCARDED_ERASE_BY_USER;
            case 6:
                return PROCESSING;
            default:
                return ERROR;
        }
    }
}
