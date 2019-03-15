package chat.tamtam.bot.domain.exception;

import lombok.Getter;

public class TamBotException extends RuntimeException {
    @Getter
    private String errorKey;
    public TamBotException(final String message, final String errorKey) {
        super(message);
        this.errorKey = errorKey;
    }
}
