package chat.tamtam.bot.custom.bot;

import chat.tamtam.bot.domain.webhook.WebHookMessageEntity;

public abstract class AbstractCustomBot {
    public abstract void processMessage(final WebHookMessageEntity message);
    public abstract String getId();
    public abstract BotType getType();
    public abstract String getToken();
}
