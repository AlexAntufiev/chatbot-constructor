package chat.tamtam.bot.domain.broadcast.message;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * CREATED -> SCHEDULED
 * CREATED -> DELETED
 * SCHEDULED -> CREATED
 * SCHEDULED -> PROCESSING
 * SCHEDULED -> DELETED
 * PROCESSING -> SENT
 * PROCESSING -> ERROR
 * PROCESSING -> ERASED_BY_SCHEDULE
 * SENT -> PROCESSING
 * SENT -> DISCARDED_ERASE_BY_USER
 * SENT -> DELETED
 * DISCARDED_ERASE_BY_USER -> SENT
 * DISCARDED_ERASE_BY_USER -> DELETED
 */

@AllArgsConstructor
public enum BroadcastMessageState {
    SCHEDULED((byte) 0),
    SENT((byte) 1),
    ERASED_BY_SCHEDULE((byte) 2),
    ERROR((byte) 3),
    CREATED((byte) 4),
    DISCARDED_ERASE_BY_USER((byte) 5),
    PROCESSING((byte) 6),
    DELETED((byte) 7),
    ;

    @Getter
    private final byte value;

    private static final Map<Byte, BroadcastMessageState> BY_ID = new HashMap<>();
    private static final EnumSet<BroadcastMessageState> REMOVABLE_STATES;
    private static final EnumSet<BroadcastMessageState> UPDATABLE_ATTACHMENT_STATES;

    static {
        REMOVABLE_STATES = EnumSet.of(
                SCHEDULED,
                SENT,
                ERASED_BY_SCHEDULE,
                ERROR,
                CREATED,
                DISCARDED_ERASE_BY_USER
        );
        UPDATABLE_ATTACHMENT_STATES = EnumSet.of(
                SCHEDULED,
                CREATED
        );
        for (BroadcastMessageState state : BroadcastMessageState.values()) {
            if (BY_ID.put(state.value, state) != null) {
                throw new IllegalArgumentException("Duplicate id=" + state.value);
            }
        }
    }

    public static boolean isRemovable(final BroadcastMessageState state) {
        return REMOVABLE_STATES.contains(state);
    }

    public static boolean isAttachmentUpdatable(final BroadcastMessageState state) {
        return UPDATABLE_ATTACHMENT_STATES.contains(state);
    }

    public static BroadcastMessageState getById(byte id) {
        return BY_ID.get(id);
    }
}
