package chat.tamtam.bot.custom.bot;

import chat.tamtam.botapi.model.Message;

public abstract class AbstractCustomBot {
    public void processMessage(Message message) {
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
