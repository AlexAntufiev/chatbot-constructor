package chat.tamtam.bot.domain.builder.validator;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum ValidatorType {
    EQUAL_TEXT((byte) 0),
    ;

    @Getter
    private final byte type;

    private static final Map<Byte, ValidatorType> BY_ID;

    static {
        BY_ID = new HashMap<>(values().length);
        for (ValidatorType type : values()) {
            if (BY_ID.containsKey(type.type)) {
                throw new IllegalStateException("Duplicate id=" + type.type);
            }
            BY_ID.put(type.type, type);
        }
    }

    public static ValidatorType getById(final byte typeId) {
        return BY_ID.get(typeId);
    }
}
