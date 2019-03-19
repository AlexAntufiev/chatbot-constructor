package chat.tamtam.bot.domain.chatchannel;

public enum ChatChannelOption {
    // @todo #CC-63 Expand ChatChannel options on demand
    CHANNEL(1),
    WRITABLE(1 << 1),
    ;

    public final int value;

    ChatChannelOption(int v) {
        value = v;
    }
}
