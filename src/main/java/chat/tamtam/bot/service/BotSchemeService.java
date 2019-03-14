package chat.tamtam.bot.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import chat.tamtam.bot.controller.Endpoints;
import chat.tamtam.bot.domain.BotSchemeEntity;
import chat.tamtam.bot.domain.BotTokenEntity;
import chat.tamtam.bot.domain.exception.NotFoundEntityException;
import chat.tamtam.bot.domain.exception.TamBotSubscriptionException;
import chat.tamtam.bot.domain.exception.TamBotUnsubscriptionException;
import chat.tamtam.bot.repository.BotSchemaRepository;
import chat.tamtam.botapi.TamTamBotAPI;
import chat.tamtam.botapi.exceptions.APIException;
import chat.tamtam.botapi.exceptions.ClientException;
import chat.tamtam.botapi.model.SimpleQueryResult;
import chat.tamtam.botapi.model.SubscriptionRequestBody;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BotSchemeService {
    private final @NonNull BotSchemaRepository botSchemaRepository;
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

    public void connect(final String authToken, int id, final BotTokenEntity tokenEntity) {
        BotSchemeEntity bot = getBotScheme(authToken, id);
        if (bot.isConnected()) {
            return;
        }
        TamTamBotAPI tamTamBotAPI = TamTamBotAPI.create(tokenEntity.getToken());
        try {
            tamTamBotAPI.getMyInfo().execute();
            SimpleQueryResult result = tamTamBotAPI
                    .subscribe(
                            new SubscriptionRequestBody(host + Endpoints.TAM_BOT_WEBHOOK + "/" + bot.getId())
                    ).execute();
            if (result.isSuccess()) {
                bot.setToken(tokenEntity.getToken());
                bot.setConnected(true);
                botSchemaRepository.save(bot);
            } else {
                throw new TamBotSubscriptionException(
                        "Can't subscribe bot with id="
                                + id
                                + " cause success="
                                + result.isSuccess()
                );
            }
        } catch (ClientException | APIException e) {
            throw new TamBotSubscriptionException(
                    "Can't subscribe bot with id="
                            + id
                            + " cause "
                            + e.getLocalizedMessage()
            );
        }
    }

    public void disconnect(final String authToken, int id) {
        BotSchemeEntity bot = getBotScheme(authToken, id);
        if (!bot.isConnected()) {
            return;
        }
        TamTamBotAPI tamTamBotAPI = TamTamBotAPI.create(bot.getToken());
        try {
            SimpleQueryResult result = tamTamBotAPI
                    .unsubscribe(host + Endpoints.TAM_BOT_WEBHOOK + "/" + bot.getId())
                    .execute();
            if (result.isSuccess()) {
                bot.setConnected(false);
                botSchemaRepository.save(bot);
            } else {
                throw new TamBotUnsubscriptionException(
                        "Can't unsubscribe bot with id="
                                + id
                                + " cause success="
                                + result.isSuccess()
                );
            }
        } catch (ClientException | APIException e) {
            throw new TamBotUnsubscriptionException(
                    "Can't unsubscribe bot with id="
                            + id
                            + " cause "
                            + e.getLocalizedMessage()
            );
        }
    }
}
