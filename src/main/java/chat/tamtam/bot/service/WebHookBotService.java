package chat.tamtam.bot.service;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import chat.tamtam.bot.custom.bot.AbstractCustomBot;
import chat.tamtam.bot.custom.bot.BotType;
import chat.tamtam.bot.custom.bot.RegistrationBot;
import chat.tamtam.bot.domain.webhook.WebHookMessageEntity;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class WebHookBotService {
    private final Map<String, Optional<BotType>> typeMap = new ConcurrentHashMap<>();
    private final Map<BotType, Optional<AbstractCustomBot>> botMap = new ConcurrentHashMap<>();

    private final RegistrationBot registrationBot;

    public void submit(
            final String botId,
            final WebHookMessageEntity message) throws NoSuchElementException {
        AbstractCustomBot bot = botMap
                .get(typeMap.get(botId)
                        .orElseThrow(NoSuchElementException::new))
                .orElseThrow(NoSuchElementException::new);
        bot.processMessage(message);
    }

    @PostConstruct
    public void insertRegBot() {
        typeMap.put(registrationBot.getId(), Optional.of(registrationBot.getType()));
        botMap.put(registrationBot.getType(), Optional.of(registrationBot));
    }
}
