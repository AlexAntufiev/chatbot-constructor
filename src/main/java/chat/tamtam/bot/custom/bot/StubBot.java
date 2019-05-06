package chat.tamtam.bot.custom.bot;

import org.springframework.stereotype.Component;

@Component
public class StubBot extends AbstractCustomBot {
    public StubBot() {
        super(null, null, null);
    }

    @Override
    public BotType getType() {
        return BotType.Stub;
    }
}
