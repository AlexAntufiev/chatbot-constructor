package chat.tamtam.bot.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import chat.tamtam.bot.domain.BotSchemaEntity;
import chat.tamtam.bot.repository.BotSchemaRepository;

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
