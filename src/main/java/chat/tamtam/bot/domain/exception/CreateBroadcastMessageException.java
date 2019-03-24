package chat.tamtam.bot.domain.exception;

import chat.tamtam.bot.service.Error;

public class CreateBroadcastMessageException extends ChatBotConstructorException {
    public CreateBroadcastMessageException(String message, Error error) {
        super(message, error);
    }
}
