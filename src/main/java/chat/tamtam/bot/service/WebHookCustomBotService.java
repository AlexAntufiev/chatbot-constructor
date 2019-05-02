package chat.tamtam.bot.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import chat.tamtam.bot.custom.bot.AbstractCustomBot;
import chat.tamtam.bot.custom.bot.BotType;
import chat.tamtam.bot.custom.bot.StubBot;
import chat.tamtam.bot.custom.bot.hockey.Hockey2019Bot;
import chat.tamtam.bot.custom.bot.registration.RegistrationBot;
import chat.tamtam.botapi.model.Update;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class WebHookCustomBotService {
    private final Map<String, BotType> typeMap = new ConcurrentHashMap<>();
    private final Map<BotType, AbstractCustomBot> botMap = new ConcurrentHashMap<>();

    private final RegistrationBot registrationBot;
    private final Hockey2019Bot hockey2019Bot;
    private final StubBot stubBot;

    public void submit(final String botId, final Update update) throws UnsupportedOperationException {
        botMap.getOrDefault(
                typeMap.getOrDefault(botId, BotType.Stub),
                stubBot
        ).process(update);
    }

    @PostConstruct
    public void insertBots() {
        // @todo #CC-173 autowire all bots here
        typeMap.put(registrationBot.getId(), registrationBot.getType());
        typeMap.put(hockey2019Bot.getId(), hockey2019Bot.getType());

        botMap.put(registrationBot.getType(), registrationBot);
        botMap.put(hockey2019Bot.getType(), hockey2019Bot);
    }
}