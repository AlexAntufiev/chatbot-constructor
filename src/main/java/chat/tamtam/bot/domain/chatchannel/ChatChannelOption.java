package chat.tamtam.bot.domain.chatchannel;

import lombok.Getter;

public enum ChatChannelOption {
    // @todo #CC-63 Expand ChatChannel options on demand
    CHANNEL(1),
    WRITABLE(1 << 1),
    ;

    @Getter
    private final int value;

    ChatChannelOption(int v) {
        value = v;
    }
}
