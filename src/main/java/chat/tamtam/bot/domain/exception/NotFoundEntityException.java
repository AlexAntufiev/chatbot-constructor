package chat.tamtam.bot.domain.exception;

import lombok.Getter;

public class NotFoundEntityException extends RuntimeException {
    @Getter
    private final String errorKey;

    public NotFoundEntityException(String message) {
        super(message);
        errorKey = null;
    }

    public NotFoundEntityException(String message, String errorKey) {
        super(message);
        this.errorKey = errorKey;
    }
}
