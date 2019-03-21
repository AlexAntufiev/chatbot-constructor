package chat.tamtam.bot.domain.exception;

import chat.tamtam.bot.service.Error;
import lombok.Getter;

public class ChatChannelStoreException extends ChatBotConstructorException {
    @Getter
    private final Long chatChannel;

    public ChatChannelStoreException(final String message, final Error error, Long chatChannel) {
        super(message, error);
        this.chatChannel = chatChannel;
    }
}
