package chat.tamtam.bot.service;

import java.util.List;

import org.springframework.stereotype.Service;

import chat.tamtam.bot.domain.BotSchemaEntity;
import chat.tamtam.bot.repository.BotSchemaRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class BotService {
    private final BotSchemaRepository botSchemaRepository;

    public boolean addBot(final BotSchemaEntity bot) {
        return false;
    }

    public boolean deleteBot(final BotSchemaEntity bot) {
        return false;
    }

    public List<BotSchemaEntity> getList(final Integer userId) {
        return botSchemaRepository.findAllByUserId(userId);
    }

}
