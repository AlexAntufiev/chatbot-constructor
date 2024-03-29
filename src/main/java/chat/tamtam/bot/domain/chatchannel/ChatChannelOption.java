package chat.tamtam.bot.domain.chatchannel;

import java.util.HashSet;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum ChatChannelOption {
    // @todo #CC-63 Expand ChatChannel options on demand
    CHANNEL(1),
    WRITABLE(1 << 1),
    ;

    @Getter
    private final int value;

    public static Set<ChatChannelOption> getOptions(int options) {
        Set<ChatChannelOption> optionsSet = new HashSet<>();
        if (hasOption(options, CHANNEL)) {
            optionsSet.add(CHANNEL);
        }
        if (hasOption(options, WRITABLE)) {
            optionsSet.add(WRITABLE);
        }
        return optionsSet;
    }

    public static int setOption(int options, boolean condition, ChatChannelOption option) {
        options |= condition ? option.value : 0;
        return options;
    }

    public static boolean hasOption(int options, ChatChannelOption option) {
        return (options & option.value) == 1;
    }
}
