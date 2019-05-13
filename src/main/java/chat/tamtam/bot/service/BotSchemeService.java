package chat.tamtam.bot.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import chat.tamtam.bot.configuration.logging.Loggable;
import chat.tamtam.bot.controller.Endpoint;
import chat.tamtam.bot.domain.bot.BotScheme;
import chat.tamtam.bot.domain.bot.TamBotEntity;
import chat.tamtam.bot.domain.builder.component.SchemeComponent;
import chat.tamtam.bot.domain.exception.ChatBotConstructorException;
import chat.tamtam.bot.domain.exception.NotFoundEntityException;
import chat.tamtam.bot.domain.response.SuccessResponse;
import chat.tamtam.bot.domain.response.SuccessResponseWrapper;
import chat.tamtam.bot.repository.BotSchemeRepository;
import chat.tamtam.bot.repository.ComponentRepository;
import chat.tamtam.bot.repository.TamBotRepository;
import chat.tamtam.bot.utils.TransactionalUtils;
import chat.tamtam.botapi.TamTamBotAPI;
import chat.tamtam.botapi.exceptions.APIException;
import chat.tamtam.botapi.exceptions.ClientException;
import chat.tamtam.botapi.model.SimpleQueryResult;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@RequiredArgsConstructor
public class BotSchemeService {
    private final @NonNull BotSchemeRepository botSchemeRepository;
    private final @NonNull TamBotRepository tamBotRepository;
    private final @NonNull ComponentRepository componentRepository;

    private final @NonNull UserService userService;

    private final @NonNull TransactionalUtils transactionalUtils;

    @Value("${tamtam.host}")
    private String host;

    @Loggable
    public BotScheme addBot(final BotScheme bot, Long userId) throws IllegalArgumentException {
        if (bot.getName().isEmpty() || bot.getId() != null) {
            throw new IllegalArgumentException("Invalid bot entity " + bot);
        }

        bot.setUserId(userId);
        return ((BotScheme) transactionalUtils
                .invokeCallable(() -> {
                    SchemeComponent resetComponent = componentRepository.save(new SchemeComponent());
//                    bot.setSchemeResetState(resetComponent.getId());
                    return botSchemeRepository.save(bot);
                }));
    }

    @Loggable
    public BotScheme saveBot(
            final BotScheme bot,
            Long userId,
            Integer id
    ) throws NoSuchElementException {
        BotScheme botScheme = botSchemeRepository.findByUserIdAndId(userId, id);
        if (botScheme == null) {
            throw new NoSuchElementException(
                    String.format("Does not exist bot with userId=%d, id=%d", userId, id)
            );
        }
        botScheme.setName(bot.getName());
        return botSchemeRepository.save(botScheme);
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
                    disconnect(tamBot, botScheme.getId());
                    tamBotRepository.deleteById(tamBot.getId());
                }
            }
            botSchemeRepository.deleteByUserIdAndId(botScheme.getUserId(), botScheme.getId());
        });
        return new SuccessResponse();
    }

    private void disconnect(final TamBotEntity tamBot, final int schemeId) {
        TamTamBotAPI tamTamBotAPI = TamTamBotAPI.create(tamBot.getToken());
        try {
            final String url = host + Endpoint.TAM_BOT + "/" + schemeId;

            SimpleQueryResult result =
                    tamTamBotAPI
                            .unsubscribe(url)
                            .execute();

            if (result.isSuccess()) {
                log.info(
                        String.format(
                                "Bot(id=%d, token=%s, botSchemeId=%d, url=%s) was unsubscribed",
                                tamBot.getId().getBotId(), tamBot.getToken(), schemeId, url
                        )
                );
            } else {
                throw new ChatBotConstructorException(
                        String.format(
                                "Can't unsubscribe bot with id=%d because success=%s",
                                schemeId, result.isSuccess()
                        ),
                        Error.TAM_SERVICE_ERROR
                );
            }
        } catch (ClientException | APIException e) {
            throw new ChatBotConstructorException(
                    String.format(
                            "Can't unsubscribe bot with id=%d because %s",
                            schemeId, e.getLocalizedMessage()
                    ),
                    Error.TAM_SERVICE_ERROR,
                    e
            );
        }
    }

    @Loggable
    public SuccessResponseWrapper<List<BotScheme>> getList(final Long userId) {
        return new SuccessResponseWrapper<>(botSchemeRepository.findAllByUserId(userId));
    }
}
