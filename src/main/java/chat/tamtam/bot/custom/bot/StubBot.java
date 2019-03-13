package chat.tamtam.bot.custom.bot;

import org.springframework.stereotype.Component;

@Component
public class StubBot extends AbstractCustomBot {
    @Override
    public BotType getType() {
        return BotType.Stub;
    }
}
