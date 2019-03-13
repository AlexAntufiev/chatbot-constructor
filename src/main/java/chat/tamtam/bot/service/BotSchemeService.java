package chat.tamtam.bot.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import chat.tamtam.bot.domain.BotSchemeEntity;
import chat.tamtam.bot.domain.exception.NoFoundEntityException;
import chat.tamtam.bot.repository.BotSchemaRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class BotSchemeService {
    private final BotSchemaRepository botSchemaRepository;
    private final UserService userService;

    public BotSchemeEntity addBot(final BotSchemeEntity bot, Integer userId) throws IllegalArgumentException {
        if (bot.getName().isEmpty() || bot.getId() != null) {
            throw new IllegalArgumentException("Invalid bot entity " + bot);
        }
        bot.setUserId(userId);
        return botSchemaRepository.save(bot);
    }

    public BotSchemeEntity saveBot(
            final BotSchemeEntity bot,
            Integer userId,
            Integer id
    ) throws NoSuchElementException {
        if (!botSchemaRepository.existsByUserIdAndId(userId, id)) {
            throw new NoSuchElementException("Does not exist bot with userId=" + userId + " and id=" + id);
        }
        bot.setUserId(userId);
        bot.setId(id);
        return botSchemaRepository.save(bot);
    }

    public BotSchemeEntity getBotScheme(String authToken, int id) throws NoSuchElementException {
        Integer userId = userService.getUserIdByToken(authToken);
        BotSchemeEntity bot = botSchemaRepository.findByUserIdAndId(userId, id);
        if (bot == null) {
            throw new NoFoundEntityException("Does not exist bot with userId=" + userId + " and id=" + id);
        } else {
            return bot;
        }
    }

    public void deleteByUserIdAndId(Integer userId, Integer id) throws NoSuchElementException {
        if (!botSchemaRepository.existsByUserIdAndId(userId, id)) {
            throw new NoSuchElementException("Does not exist bot with userId=" + userId + " and id=" + id);
        }
        botSchemaRepository.deleteByUserIdAndId(userId, id);
    }

    public boolean deleteBot(final BotSchemeEntity bot) {
        return false;
    }

    public List<BotSchemeEntity> getList(final Integer userId) {
        return botSchemaRepository.findAllByUserId(userId);
    }

}
