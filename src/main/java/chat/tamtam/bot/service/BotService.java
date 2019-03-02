package chat.tamtam.bot.service;

import chat.tamtam.bot.domain.BotSchemaEntity;
import chat.tamtam.bot.repository.BotSchemaRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class BotService {
    private BotSchemaRepository botSchemaRepository;

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
