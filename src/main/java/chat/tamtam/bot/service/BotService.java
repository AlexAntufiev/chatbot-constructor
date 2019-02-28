package chat.tamtam.bot.service;

import chat.tamtam.bot.domain.BotSchemaEntity;
import chat.tamtam.bot.repository.BotSchemaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BotService {
    @Autowired
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
