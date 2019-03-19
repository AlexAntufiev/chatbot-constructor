package chat.tamtam.bot.domain.exception;

import lombok.Getter;

public class ChatBotConstructorException extends RuntimeException {
    @Getter
    private String errorKey;

    public ChatBotConstructorException(final String message, final String errorKey) {
        super(message);
        this.errorKey = errorKey;
    }
}
