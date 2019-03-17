package chat.tamtam.bot.domain.exception;

import java.util.List;

import lombok.Getter;

public class ChannelStoreException extends TamBotException {
    @Getter
    private List<Long> channels;

    public ChannelStoreException(final String message, final String errorKey, final List<Long> channels) {
        super(message, errorKey);
        this.channels = channels;
    }
}
