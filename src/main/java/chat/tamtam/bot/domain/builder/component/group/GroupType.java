package chat.tamtam.bot.domain.builder.component.group;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum GroupType {
    DEFAULT((byte) 0),
    VOTE((byte) 1),
    ;

    @Getter
    private final byte type;

    private static final Map<Byte, GroupType> BY_ID;

    static {
        BY_ID = new HashMap<>(values().length);
        for (GroupType type
                : values()) {
            if (BY_ID.containsKey(type.type)) {
                throw new IllegalStateException("Duplicate id=" + type.type);
            }
            BY_ID.put(type.type, type);
        }
    }

    public static GroupType getById(final byte typeId) {
        return BY_ID.get(typeId);
    }
}
