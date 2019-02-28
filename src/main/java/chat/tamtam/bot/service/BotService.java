package chat.tamtam.bot.service;

import chat.tamtam.bot.domain.BotSchemaEntity;
import chat.tamtam.bot.repository.BotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BotService {
    @Autowired
    private BotRepository botRepository;

    public boolean addBot(final BotSchemaEntity bot) {
        return false;
    }

    public boolean deleteBot(final BotSchemaEntity bot) {
        return false;
    }

    public List<BotSchemaEntity> getList(final Integer userId) {
        return botRepository.findAllByUserId(userId);
    }

}
