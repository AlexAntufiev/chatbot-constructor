package chat.tamtam.bot.custom.bot;

import chat.tamtam.botapi.model.Update;

public abstract class AbstractCustomBot {
    public void process(Update update) {
        throw new UnsupportedOperationException("from " + getType());
    };

    public String getId() {
        throw new UnsupportedOperationException("from " + getType());
    };

    public abstract BotType getType();

    public String getToken() {
        throw new UnsupportedOperationException("from " + getType());
    };
}
