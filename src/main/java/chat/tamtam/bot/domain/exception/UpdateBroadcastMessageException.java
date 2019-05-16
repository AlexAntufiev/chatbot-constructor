package chat.tamtam.bot.domain.exception;

import chat.tamtam.bot.service.Error;

public class UpdateBroadcastMessageException extends ChatBotConstructorException {
    public UpdateBroadcastMessageException(final String message, final Error error) {
        super(message, error);
    }
}
