package chat.tamtam.bot.domain.exception;

import chat.tamtam.bot.service.Error;

public class GenerateUploadLinkException extends ChatBotConstructorException {
    public GenerateUploadLinkException(
            final String message,
            final Error error
    ) {
        super(message, error);
    }
}
