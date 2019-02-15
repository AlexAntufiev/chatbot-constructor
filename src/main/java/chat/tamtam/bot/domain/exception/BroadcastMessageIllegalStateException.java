package chat.tamtam.bot.domain.exception;

import chat.tamtam.bot.service.Error;

public class BroadcastMessageIllegalStateException extends ChatBotConstructorException {

    public BroadcastMessageIllegalStateException(final String message, final Error error) {
        super(message, error);
    }
}
