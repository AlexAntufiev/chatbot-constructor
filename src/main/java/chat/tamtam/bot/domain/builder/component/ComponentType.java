package chat.tamtam.bot.domain.builder.component;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum ComponentType {
    INPUT((byte) 0),
    INFO((byte) 1),
    RESET((byte) 2)
    ;

    @Getter
    private final byte type;

    private static final Map<Byte, ComponentType> BY_ID;

    static {
        BY_ID = new HashMap<>(values().length);
        for (ComponentType type
                : values()) {
            if (BY_ID.containsKey(type.type)) {
                throw new IllegalStateException("Duplicate id=" + type.type);
            }
            BY_ID.put(type.type, type);
        }
    }

    public static ComponentType getById(final byte typeId) {
        return BY_ID.get(typeId);
    }
}
