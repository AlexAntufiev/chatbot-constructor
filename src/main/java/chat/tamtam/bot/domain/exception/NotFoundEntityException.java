package chat.tamtam.bot.domain.exception;

import chat.tamtam.bot.service.Error;
import lombok.Getter;

public class NotFoundEntityException extends RuntimeException {
    @Getter
    private final String errorKey;

    public NotFoundEntityException(String message) {
        super(message);
        errorKey = null;
    }

    public NotFoundEntityException(String message, Error error) {
        super(message);
        this.errorKey = error.getErrorKey();
    }
}
