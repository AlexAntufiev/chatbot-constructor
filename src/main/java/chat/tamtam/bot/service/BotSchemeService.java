package chat.tamtam.bot.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import chat.tamtam.bot.domain.BotSchemeEntity;
import chat.tamtam.bot.domain.exception.NotFoundEntityException;
import chat.tamtam.bot.repository.BotSchemaRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BotSchemeService {
    private final @NonNull BotSchemaRepository botSchemaRepository;
    private final @NonNull UserService userService;

    public BotSchemeEntity addBot(final BotSchemeEntity bot, Long userId) throws IllegalArgumentException {
        if (bot.getName().isEmpty() || bot.getId() != null) {
            throw new IllegalArgumentException("Invalid bot entity " + bot);
        }
        bot.setUserId(userId);
        return botSchemaRepository.save(bot);
    }

    public BotSchemeEntity saveBot(
            final BotSchemeEntity bot,
            Long userId,
            Integer id
    ) throws NoSuchElementException {
        if (!botSchemaRepository.existsByUserIdAndId(userId, id)) {
            throw new NoSuchElementException("Does not exist bot with userId=" + userId + " and id=" + id);
        }
        bot.setUserId(userId);
        bot.setId(id);
        return botSchemaRepository.save(bot);
    }

    public BotSchemeEntity getBotScheme(String authToken, int botSchemeId) throws NotFoundEntityException {
        Long userId = userService.getUserIdByToken(authToken);
        BotSchemeEntity bot = botSchemaRepository.findByUserIdAndId(userId, botSchemeId);
        if (bot == null) {
            throw new NotFoundEntityException("Does not exist bot with userId=" + userId + " and id=" + botSchemeId);
        } else {
            return bot;
        }
    }

    public void deleteByUserIdAndId(Long userId, Integer id) throws NoSuchElementException {
        if (!botSchemaRepository.existsByUserIdAndId(userId, id)) {
            throw new NoSuchElementException("Does not exist bot with userId=" + userId + " and id=" + id);
        }
        botSchemaRepository.deleteByUserIdAndId(userId, id);
    }

    public boolean deleteBot(final BotSchemeEntity bot) {
        return false;
    }

    public List<BotSchemeEntity> getList(final Long userId) {
        return botSchemaRepository.findAllByUserId(userId);
    }
}
