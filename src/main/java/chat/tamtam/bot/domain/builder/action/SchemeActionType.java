package chat.tamtam.bot.domain.builder.action;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum SchemeActionType {
    STORE_VOTE_ENTRY((byte) 0),
    PERSIST_VOTE_TO_TABLE(((byte) 1)),
    ;

    @Getter
    private final byte type;

    private static final Map<Byte, SchemeActionType> BY_ID;

    static {
        BY_ID = new HashMap<>(values().length);
        for (SchemeActionType type
                : values()) {
            if (BY_ID.containsKey(type.type)) {
                throw new IllegalStateException("Duplicate id=" + type.type);
            }
            BY_ID.put(type.type, type);
        }
    }

    public static SchemeActionType getById(final byte typeId) {
        return BY_ID.get(typeId);
    }
}
