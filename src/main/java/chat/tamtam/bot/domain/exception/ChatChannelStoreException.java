package chat.tamtam.bot.domain.exception;

import java.util.List;

import lombok.Getter;

public class ChatChannelStoreException extends TamBotException {
    @Getter
    private List<Long> chat;

    public ChatChannelStoreException(final String message, final String errorKey, final List<Long> chat) {
        super(message, errorKey);
        this.chat = chat;
    }
}
