package chat.tamtam.bot.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import chat.tamtam.bot.controller.Endpoints;
import chat.tamtam.bot.domain.BotSchemaInfoEntity;
import chat.tamtam.bot.domain.BotSchemeEntity;
import chat.tamtam.bot.domain.exception.NotFoundEntityException;
import chat.tamtam.bot.domain.exception.TamBotSubscriptionException;
import chat.tamtam.bot.domain.response.BotSubscriptionSuccessEntity;
import chat.tamtam.bot.repository.BotSchemaInfoRepository;
import chat.tamtam.bot.repository.BotSchemaRepository;
import chat.tamtam.botapi.TamTamBotAPI;
import chat.tamtam.botapi.exceptions.APIException;
import chat.tamtam.botapi.exceptions.ClientException;
import chat.tamtam.botapi.model.SimpleQueryResult;
import chat.tamtam.botapi.model.SubscriptionRequestBody;
import chat.tamtam.botapi.model.UserWithPhoto;
import io.micrometer.core.lang.Nullable;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BotSchemeService {
    private final @NonNull BotSchemaRepository botSchemaRepository;
    private final @NonNull BotSchemaInfoRepository botSchemaInfoRepository;
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

    @Nullable
    public BotSchemaInfoEntity getBotSchemeInfo(final Long id) {
        return botSchemaInfoRepository.findByBotId(id);
    }

    private BotSchemaInfoEntity fetchBotSchemaInfo(TamTamBotAPI tamTamBotAPI) throws ClientException, APIException {
        UserWithPhoto userWithPhoto = tamTamBotAPI.getMyInfo().execute();
        BotSchemaInfoEntity botInfo = getBotSchemeInfo(userWithPhoto.getUserId());
        if (botInfo == null) {
            BotSchemaInfoEntity botSchemaInfoEntity = new BotSchemaInfoEntity(userWithPhoto);
            botSchemaInfoRepository.save(botSchemaInfoEntity);
            return botSchemaInfoEntity;
        } else {
            botInfo.update(userWithPhoto);
            botSchemaInfoRepository.save(botInfo);
            return botInfo;
        }
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
        if (bot.isConnected()) {
            throw new TamBotSubscriptionException(
                    "Can't subscribe bot with id="
                            + id
                            + " cause bot is subscribed already",
                    Errors.TAM_BOT_SUBSCRIBED_ALREADY
            );
        }
        TamTamBotAPI tamTamBotAPI = TamTamBotAPI.create(botToken);
        try {
            BotSchemaInfoEntity botInfo = fetchBotSchemaInfo(tamTamBotAPI);
            String webHookUrl = host + Endpoints.TAM_BOT_WEBHOOK + "/" + bot.getId();
            SimpleQueryResult result = tamTamBotAPI
                    .subscribe(
                            new SubscriptionRequestBody(webHookUrl)
                    ).execute();
            if (result.isSuccess()) {
                bot.setToken(botToken);
                bot.setConnected(true);
                bot.setWebHookUrl(webHookUrl);
                botSchemaRepository.save(bot);
                return new BotSubscriptionSuccessEntity(botInfo);
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
        if (!bot.isConnected()) {
            throw new TamBotSubscriptionException(
                    "Can't unsubscribe bot with id="
                            + id
                            + " cause bot was not subscribed",
                    Errors.TAM_BOT_UNSUBSCRIBED_ALREADY
            );
        }
        TamTamBotAPI tamTamBotAPI = TamTamBotAPI.create(bot.getToken());
        try {
            BotSchemaInfoEntity botInfo = fetchBotSchemaInfo(tamTamBotAPI);
            SimpleQueryResult result = tamTamBotAPI
                    .unsubscribe(bot.getWebHookUrl())
                    .execute();
            if (result.isSuccess()) {
                bot.setConnected(false);
                botSchemaRepository.save(bot);
                return new BotSubscriptionSuccessEntity(botInfo);
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
