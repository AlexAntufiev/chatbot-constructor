package chat.tamtam.bot.domain.exception;

public class NoFoundEntityException extends RuntimeException {

    public NoFoundEntityException(String message) {
        super(message);
    }
}
