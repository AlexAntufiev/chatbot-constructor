package chat.tamtam.bot.service;

import chat.tamtam.bot.repository.BotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BotService {

    @Autowired
    private BotRepository botRepository;

}
