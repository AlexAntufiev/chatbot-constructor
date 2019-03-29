package chat.tamtam.bot.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import chat.tamtam.bot.configuration.logging.Loggable;
import chat.tamtam.bot.domain.bot.BotSchemeEntity;
import chat.tamtam.bot.domain.bot.TamBotEntity;
import chat.tamtam.bot.domain.exception.NotFoundEntityException;
import chat.tamtam.bot.domain.response.SuccessResponse;
import chat.tamtam.bot.domain.response.SuccessResponseWrapper;
import chat.tamtam.bot.repository.BotSchemaRepository;
import chat.tamtam.bot.repository.TamBotRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BotSchemeService {
    private final @NonNull BotSchemaRepository botSchemaRepository;
    private final @NonNull TamBotRepository tamBotRepository;

    private final @NonNull UserService userService;

    private final @NonNull TransactionalUtils transactionalUtils;

    @Loggable
    public BotSchemeEntity addBot(final BotSchemeEntity bot, Long userId) throws IllegalArgumentException {
        if (bot.getName().isEmpty() || bot.getId() != null) {
            throw new IllegalArgumentException("Invalid bot entity " + bot);
        }
        bot.setUserId(userId);
        return botSchemaRepository.save(bot);
    }

    @Loggable
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

    @Loggable
    public BotSchemeEntity getBotScheme(String authToken, int botSchemeId) throws NotFoundEntityException {
        Long userId = userService.getUserIdByToken(authToken);
        BotSchemeEntity bot = botSchemaRepository.findByUserIdAndId(userId, botSchemeId);
        if (bot == null) {
            throw new NotFoundEntityException("Does not exist bot with userId=" + userId + " and id=" + botSchemeId);
        } else {
            return bot;
        }
    }

    @Loggable
    public void deleteByUserIdAndId(Long userId, Integer id) throws NoSuchElementException {
        if (!botSchemaRepository.existsByUserIdAndId(userId, id)) {
            throw new NoSuchElementException("Does not exist bot with userId=" + userId + " and id=" + id);
        }
        botSchemaRepository.deleteByUserIdAndId(userId, id);
    }

    @Loggable
    public SuccessResponse deleteBot(
            final String authToken,
            final int botSchemeId
    ) {
        BotSchemeEntity botScheme = getBotScheme(authToken, botSchemeId);
        transactionalUtils.invokeRunnable(() -> {
            if (botScheme.getBotId() != null) {
                TamBotEntity tamBot = tamBotRepository.findById(
                        new TamBotEntity.Id(botScheme.getBotId(), botScheme.getUserId())
                );
                if (tamBot != null) {
                    tamBotRepository.deleteById(tamBot.getId());
                }
            }
            botSchemaRepository.deleteByUserIdAndId(botScheme.getUserId(), botScheme.getId());
        });
        return new SuccessResponse();
    }

    @Loggable
    public SuccessResponseWrapper<List<BotSchemeEntity>> getList(final Long userId) {
        return new SuccessResponseWrapper<>(botSchemaRepository.findAllByUserId(userId));
    }
}
