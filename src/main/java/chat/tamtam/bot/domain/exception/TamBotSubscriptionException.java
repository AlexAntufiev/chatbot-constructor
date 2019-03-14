package chat.tamtam.bot.domain.exception;

import lombok.Getter;

public class TamBotSubscriptionException extends RuntimeException {
    @Getter
    private String errorKey;
    public TamBotSubscriptionException(final String message, final String errorKey) {
        super(message);
        this.errorKey = errorKey;
    }
}
