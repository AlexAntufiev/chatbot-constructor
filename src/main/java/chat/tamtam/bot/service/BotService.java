package chat.tamtam.bot.service;

import chat.tamtam.bot.domain.BotEntity;
import chat.tamtam.bot.repository.BotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BotService {
    @Autowired
    private BotRepository botRepository;

    public boolean addBot(final BotEntity bot) {
        BotEntity botEntity = botRepository.save(bot);
        return botEntity != null;
    }

    public List<BotEntity> getList(final Integer userId) {
        return botRepository.findAllByUserId(userId);
    }

}
