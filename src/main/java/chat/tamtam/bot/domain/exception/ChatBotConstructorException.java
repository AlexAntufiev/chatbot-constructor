package chat.tamtam.bot.domain.exception;

import chat.tamtam.bot.service.Error;
import lombok.Getter;

public class ChatBotConstructorException extends RuntimeException {
    @Getter
    private String errorKey;

    public ChatBotConstructorException(final String message, final Error error) {
        super(message);
        if (error != null) {
            errorKey = error.getErrorKey();
        }
    }
}
