package chat.tamtam.bot.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import chat.tamtam.bot.controller.Endpoints;
import chat.tamtam.bot.domain.BotSchemeEntity;
import chat.tamtam.bot.domain.TamBotEntity;
import chat.tamtam.bot.domain.TamBotId;
import chat.tamtam.bot.domain.exception.NotFoundEntityException;
import chat.tamtam.bot.domain.exception.TamBotSubscriptionException;
import chat.tamtam.bot.domain.response.BotSubscriptionSuccessEntity;
import chat.tamtam.bot.repository.BotSchemaRepository;
import chat.tamtam.bot.repository.TamBotRepository;
import chat.tamtam.botapi.TamTamBotAPI;
import chat.tamtam.botapi.exceptions.APIException;
import chat.tamtam.botapi.exceptions.ClientException;
import chat.tamtam.botapi.model.SimpleQueryResult;
import chat.tamtam.botapi.model.SubscriptionRequestBody;
import chat.tamtam.botapi.model.UserWithPhoto;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BotSchemeService {
    private final @NonNull BotSchemaRepository botSchemaRepository;
    private final @NonNull TamBotRepository tamBotRepository;
    private final @NonNull UserService userService;

    @Value("${tamtam.host}")
    private String host;

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

    public BotSchemeEntity getBotScheme(String authToken, int id) throws NotFoundEntityException {
        Integer userId = userService.getUserIdByToken(authToken);
        BotSchemeEntity bot = botSchemaRepository.findByUserIdAndId(userId, id);
        if (bot == null) {
            throw new NotFoundEntityException("Does not exist bot with userId=" + userId + " and id=" + id);
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

    private TamBotEntity fetchTamBot(
            final TamTamBotAPI tamTamBotAPI,
            final Integer userId,
            final String token) throws ClientException, APIException {
        UserWithPhoto userWithPhoto = tamTamBotAPI.getMyInfo().execute();
        return new TamBotEntity(userId, token, userWithPhoto);
    }

    public BotSubscriptionSuccessEntity connect(final String authToken, int id, final String botToken) {
        if (StringUtils.isEmpty(botToken)) {
            throw new TamBotSubscriptionException(
                    "Can't subscribe bot with id="
                            + id
                            + " cause bot is subscribed already",
                    Errors.TAM_BOT_TOKEN_EMPTY
            );
        }
        BotSchemeEntity bot = getBotScheme(authToken, id);
        if (bot.getBotId() != null) {
            throw new TamBotSubscriptionException(
                    "Can't subscribe bot with id="
                            + id
                            + " cause bot is subscribed already",
                    Errors.TAM_BOT_SUBSCRIBED_ALREADY
            );
        }
        TamTamBotAPI tamTamBotAPI = TamTamBotAPI.create(botToken);
        try {
            TamBotEntity tamBot = fetchTamBot(tamTamBotAPI, bot.getUserId(), botToken);
            SimpleQueryResult result = tamTamBotAPI
                    .subscribe(
                            new SubscriptionRequestBody(host + Endpoints.TAM_BOT_WEBHOOK + "/" + bot.getId())
                    ).execute();
            if (result.isSuccess()) {
                bot.setBotId(tamBot.getId().getBotId());
                // @todo CC-52 wrap save operations into transaction
                tamBotRepository.save(tamBot);
                botSchemaRepository.save(bot);
                return new BotSubscriptionSuccessEntity(tamBot);
            } else {
                throw new TamBotSubscriptionException(
                        "Can't subscribe bot with id="
                                + id
                                + " cause success="
                                + result.isSuccess(),
                        Errors.TAM_SERVICE_ERROR
                );
            }
        } catch (ClientException | APIException e) {
            if (!StringUtils.isEmpty(e.getMessage())
                    && e.getMessage().equals("API exception verify.token: Invalid access_token")) {
                throw new TamBotSubscriptionException(
                        "Can't subscribe bot with id="
                                + id
                                + " cause "
                                + e.getLocalizedMessage(),
                        Errors.TAM_BOT_TOKEN_INCORRECT
                );
            }
            throw new TamBotSubscriptionException(
                    "Can't subscribe bot with id="
                            + id
                            + " cause "
                            + e.getLocalizedMessage(),
                    Errors.TAM_SERVICE_ERROR
            );
        }
    }

    public BotSubscriptionSuccessEntity disconnect(final String authToken, int id) {
        BotSchemeEntity bot = getBotScheme(authToken, id);
        if (bot.getBotId() == null) {
            throw new TamBotSubscriptionException(
                    "Can't unsubscribe bot with id="
                            + id
                            + " cause bot was not subscribed",
                    Errors.TAM_BOT_UNSUBSCRIBED_ALREADY
            );
        }
        TamBotEntity tamBot = tamBotRepository
                .findById(new TamBotId(bot.getBotId(), bot.getUserId()));
        TamTamBotAPI tamTamBotAPI = TamTamBotAPI.create(tamBot.getToken());
        try {
            SimpleQueryResult result = tamTamBotAPI
                    .unsubscribe(host + Endpoints.TAM_BOT_WEBHOOK + "/" + bot.getId())
                    .execute();
            if (result.isSuccess()) {
                bot.setBotId(null);
                // @todo CC-52 wrap delete and save operations into transaction
                tamBotRepository.deleteById(new TamBotId(bot.getBotId(), bot.getUserId()));
                botSchemaRepository.save(bot);
                return new BotSubscriptionSuccessEntity(tamBot);
            } else {
                throw new TamBotSubscriptionException(
                        "Can't unsubscribe bot with id="
                                + id
                                + " cause success="
                                + result.isSuccess(),
                        Errors.TAM_SERVICE_ERROR
                );
            }
        } catch (ClientException | APIException e) {
            throw new TamBotSubscriptionException(
                    "Can't unsubscribe bot with id="
                            + id
                            + " cause "
                            + e.getLocalizedMessage(),
                    Errors.TAM_SERVICE_ERROR
            );
        }
    }
}
