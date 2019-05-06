package chat.tamtam.bot.custom.bot;

import org.springframework.stereotype.Component;

@Component
public class StubBot extends AbstractCustomBot {
    public StubBot() {
        super(null, null, null, null);
    }

    @Override
    public BotType getType() {
        return BotType.Stub;
    }

    @Override
    protected void subscribe() { }

    @Override
    public void unsubscribe() { }
}
