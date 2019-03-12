package chat.tamtam.bot.custom.bot;

import chat.tamtam.botapi.model.Message;

public abstract class AbstractCustomBot {
    public abstract void processMessage(Message message);

    public abstract String getId();

    public abstract BotType getType();

    public abstract String getToken();
}
