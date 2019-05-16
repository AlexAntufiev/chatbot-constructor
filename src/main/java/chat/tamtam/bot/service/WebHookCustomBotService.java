package chat.tamtam.bot.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import chat.tamtam.bot.custom.bot.AbstractCustomBot;
import chat.tamtam.bot.custom.bot.BotType;
import chat.tamtam.bot.custom.bot.StubBot;
import chat.tamtam.botapi.model.Update;

@Service
public class WebHookCustomBotService {
    private final Map<String, BotType> typeMap = new ConcurrentHashMap<>();
    private final Map<BotType, AbstractCustomBot> botMap = new ConcurrentHashMap<>();

    private final StubBot stubBot;

    public WebHookCustomBotService(@Autowired final List<AbstractCustomBot> bots, final StubBot stubBot) {
        this.stubBot = stubBot;
        bots.stream()
                .filter(bot -> bot.getType() != BotType.Stub)
                .forEach(bot -> {
                    typeMap.put(bot.getId(), bot.getType());
                    botMap.put(bot.getType(), bot);
                });
    }

    public void submit(final String botId, final Update update) throws UnsupportedOperationException {
        botMap.getOrDefault(
                typeMap.getOrDefault(botId, BotType.Stub),
                stubBot
        ).process(update);
    }
}
