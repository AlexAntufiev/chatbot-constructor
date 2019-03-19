package chat.tamtam.bot.domain.exception;

public class CreateBroadcastMessageException extends ChatBotConstructorException {
    public CreateBroadcastMessageException(String message, String errorKey) {
        super(message, errorKey);
    }
}
