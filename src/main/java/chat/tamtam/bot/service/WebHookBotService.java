package chat.tamtam.bot.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import chat.tamtam.bot.configuration.logging.Loggable;
import chat.tamtam.bot.custom.bot.AbstractCustomBot;
import chat.tamtam.bot.custom.bot.BotType;
import chat.tamtam.bot.custom.bot.RegistrationBot;
import chat.tamtam.bot.custom.bot.StubBot;
import chat.tamtam.botapi.model.Message;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class WebHookBotService {
    private final Map<String, BotType> typeMap = new ConcurrentHashMap<>();
    private final Map<BotType, AbstractCustomBot> botMap = new ConcurrentHashMap<>();

    private final RegistrationBot registrationBot;
    private final StubBot stubBot;

    @Loggable
    public void submit(
            final String botId,
            final Message message
    ) throws UnsupportedOperationException {
        botMap.getOrDefault(
                typeMap.getOrDefault(
                        botId,
                        BotType.Stub
                ),
                stubBot
        ).processMessage(message);
    }

    @Loggable
    @PostConstruct
    public void insertRegBot() {
        typeMap.put(registrationBot.getId(), registrationBot.getType());
        botMap.put(registrationBot.getType(), registrationBot);
    }
}
