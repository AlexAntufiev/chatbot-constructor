package chat.tamtam.bot.domain.exception;

import lombok.Getter;

public class BroadcastMessageIllegalStateException extends IllegalStateException {
    @Getter
    private final String errorKey;

    public BroadcastMessageIllegalStateException(final IllegalStateException ex, final String errorKey) {
        super(ex.getLocalizedMessage());
        this.errorKey = errorKey;
    }
}
