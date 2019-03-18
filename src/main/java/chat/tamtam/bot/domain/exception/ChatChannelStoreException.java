package chat.tamtam.bot.domain.exception;

import lombok.Getter;

public class ChatChannelStoreException extends TamBotException {
    @Getter
    private final Long chatChannel;

    public ChatChannelStoreException(final String message, final String errorKey, Long chatChannel) {
        super(message, errorKey);
        this.chatChannel = chatChannel;
    }
}
