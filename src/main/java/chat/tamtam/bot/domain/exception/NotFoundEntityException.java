package chat.tamtam.bot.domain.exception;

import chat.tamtam.bot.service.Error;

public class NotFoundEntityException extends ChatBotConstructorException {

    public NotFoundEntityException(String message) {
        super(message, null);
    }

    public NotFoundEntityException(String message, Error error) {
        super(message, error);
    }
}
