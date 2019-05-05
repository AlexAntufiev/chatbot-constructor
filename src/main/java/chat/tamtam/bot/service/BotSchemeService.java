package chat.tamtam.bot.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import chat.tamtam.bot.configuration.logging.Loggable;
import chat.tamtam.bot.domain.bot.BotScheme;
import chat.tamtam.bot.domain.bot.TamBotEntity;
import chat.tamtam.bot.domain.exception.NotFoundEntityException;
import chat.tamtam.bot.domain.response.SuccessResponse;
import chat.tamtam.bot.domain.response.SuccessResponseWrapper;
import chat.tamtam.bot.repository.BotSchemeRepository;
import chat.tamtam.bot.repository.TamBotRepository;
import chat.tamtam.bot.utils.TransactionalUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BotSchemeService {
    private final @NonNull BotSchemeRepository botSchemeRepository;
    private final @NonNull TamBotRepository tamBotRepository;

    private final @NonNull UserService userService;

    private final @NonNull TransactionalUtils transactionalUtils;

    @Loggable
    public BotScheme addBot(final BotScheme bot, Long userId) throws IllegalArgumentException {
        if (bot.getName().isEmpty() || bot.getId() != null) {
            throw new IllegalArgumentException("Invalid bot entity " + bot);
        }
        bot.setUserId(userId);
        return botSchemeRepository.save(bot);
    }

    @Loggable
    public BotScheme saveBot(
            final BotScheme bot,
            Long userId,
            Integer id
    ) throws NoSuchElementException {
        if (!botSchemeRepository.existsByUserIdAndId(userId, id)) {
            throw new NoSuchElementException("Does not exist bot with userId=" + userId + " and id=" + id);
        }
        bot.setUserId(userId);
        bot.setId(id);
        return botSchemeRepository.save(bot);
    }

    @Loggable
    public BotScheme getBotScheme(String authToken, int botSchemeId) throws NotFoundEntityException {
        Long userId = userService.getUserIdByToken(authToken);
        BotScheme bot = botSchemeRepository.findByUserIdAndId(userId, botSchemeId);
        if (bot == null) {
            throw new NotFoundEntityException("Does not exist bot with userId=" + userId + " and id=" + botSchemeId);
        } else {
            return bot;
        }
    }

    @Loggable
    public void deleteByUserIdAndId(Long userId, Integer id) throws NoSuchElementException {
        if (!botSchemeRepository.existsByUserIdAndId(userId, id)) {
            throw new NoSuchElementException("Does not exist bot with userId=" + userId + " and id=" + id);
        }
        botSchemeRepository.deleteByUserIdAndId(userId, id);
    }

    @Loggable
    public SuccessResponse deleteBot(
            final String authToken,
            final int botSchemeId
    ) {
        BotScheme botScheme = getBotScheme(authToken, botSchemeId);
        transactionalUtils.invokeRunnable(() -> {
            if (botScheme.getBotId() != null) {
                TamBotEntity tamBot = tamBotRepository.findById(
                        new TamBotEntity.Id(botScheme.getBotId(), botScheme.getUserId())
                );
                if (tamBot != null) {
                    tamBotRepository.deleteById(tamBot.getId());
                }
            }
            botSchemeRepository.deleteByUserIdAndId(botScheme.getUserId(), botScheme.getId());
        });
        return new SuccessResponse();
    }

    @Loggable
    public SuccessResponseWrapper<List<BotScheme>> getList(final Long userId) {
        return new SuccessResponseWrapper<>(botSchemeRepository.findAllByUserId(userId));
    }
}
