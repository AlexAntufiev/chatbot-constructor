package chat.tamtam.bot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import chat.tamtam.bot.controller.Endpoint;
import chat.tamtam.bot.domain.bot.BotSchemeEntity;
import chat.tamtam.bot.domain.bot.TamBotEntity;
import chat.tamtam.bot.domain.exception.ChatBotConstructorException;
import chat.tamtam.bot.domain.exception.GenerateUploadLinkException;
import chat.tamtam.bot.domain.exception.NotFoundEntityException;
import chat.tamtam.bot.domain.response.SuccessResponse;
import chat.tamtam.bot.domain.response.SuccessResponseWrapper;
import chat.tamtam.bot.repository.BotSchemaRepository;
import chat.tamtam.bot.repository.TamBotRepository;
import chat.tamtam.botapi.TamTamBotAPI;
import chat.tamtam.botapi.exceptions.APIException;
import chat.tamtam.botapi.exceptions.ClientException;
import chat.tamtam.botapi.model.SimpleQueryResult;
import chat.tamtam.botapi.model.SubscriptionRequestBody;
import chat.tamtam.botapi.model.UploadEndpoint;
import chat.tamtam.botapi.model.UploadType;
import chat.tamtam.botapi.model.UserWithPhoto;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TamBotService {
    private final TamBotRepository tamBotRepository;
    private final BotSchemaRepository botSchemaRepository;
    private final UserService userService;
    private final BotSchemeService botSchemeService;
    private final TransactionalUtils transactionalUtils;

    @Value("${tamtam.host}")
    private String host;

    private TamBotEntity getTamBot(final Long tamBotId, long userId) {
        if (tamBotId == null) {
            throw new ChatBotConstructorException(
                    "Can't find tam bot info with id="
                            + tamBotId
                            + " cause entity does not exist",
                    Error.TAM_BOT_NOT_SUBSCRIBED
            );
        }
        TamBotEntity tamBot = tamBotRepository.findById(new TamBotEntity.Id(tamBotId, userId));
        if (tamBot == null) {
            throw new NotFoundEntityException(
                    "Can't find tam bot with id="
                            + tamBotId

            );
        }
        return tamBot;
    }

    private TamBotEntity fetchTamBotInfo(
            final TamTamBotAPI tamTamBotAPI,
            final Long userId,
            final String token
    ) throws ClientException, APIException {
        UserWithPhoto userWithPhoto = tamTamBotAPI.getMyInfo().execute();
        return new TamBotEntity(userId, token, userWithPhoto);
    }

    public TamBotEntity status(final String authToken, final int botSchemeId) {
        long userId = userService.getUserIdByToken(authToken);
        BotSchemeEntity botScheme = botSchemeService.getBotScheme(authToken, botSchemeId);
        return getTamBot(botScheme.getBotId(), userId);
    }

    public TamBotEntity getTamBot(final BotSchemeEntity botScheme) {
        if (botScheme.getBotId() == null) {
            throw new ChatBotConstructorException(
                    "Tam bot for bot scheme with id="
                            + botScheme.getId()
                            + "was not subscribed",
                    Error.TAM_BOT_NOT_SUBSCRIBED
            );
        }
        TamBotEntity tamBot = tamBotRepository.findById(
                new TamBotEntity.Id(botScheme.getBotId(), botScheme.getUserId())
        );
        if (tamBot == null) {
            throw new NotFoundEntityException(
                    "Does not exist tam bot with botId="
                            + botScheme.getBotId()
            );
        }
        return tamBot;
    }

    public SuccessResponse connect(final String authToken, int id, final String botToken) {
        if (StringUtils.isEmpty(botToken)) {
            throw new ChatBotConstructorException(
                    "Can't subscribe bot with id="
                            + id
                            + " cause bot is subscribed already",
                    Error.TAM_BOT_TOKEN_EMPTY
            );
        }
        BotSchemeEntity bot = botSchemeService.getBotScheme(authToken, id);
        if (bot.getBotId() != null) {
            throw new ChatBotConstructorException(
                    "Can't subscribe bot with id="
                            + id
                            + " cause bot is subscribed already",
                    Error.TAM_BOT_SUBSCRIBED_ALREADY
            );
        }
        TamTamBotAPI tamTamBotAPI = TamTamBotAPI.create(botToken);
        try {
            TamBotEntity tamBot = fetchTamBotInfo(tamTamBotAPI, bot.getUserId(), botToken);
            SimpleQueryResult result = tamTamBotAPI
                    .subscribe(
                            new SubscriptionRequestBody(host + Endpoint.TAM_BOT + "/" + bot.getId())
                    ).execute();
            if (result.isSuccess()) {
                bot.setBotId(tamBot.getId().getBotId());
                transactionalUtils
                        .transactionalOperation(() -> {
                            tamBotRepository.save(tamBot);
                            botSchemaRepository.save(bot);
                        });
                return new SuccessResponseWrapper<>(tamBot);
            } else {
                throw new ChatBotConstructorException(
                        "Can't subscribe bot with id="
                                + id
                                + " cause success="
                                + result.isSuccess(),
                        Error.TAM_SERVICE_ERROR
                );
            }
        } catch (ClientException | APIException e) {
            if (!StringUtils.isEmpty(e.getMessage())
                    && e.getMessage().equals("API exception verify.token: Invalid access_token")) {
                throw new ChatBotConstructorException(
                        "Can't subscribe bot with id="
                                + id
                                + " cause "
                                + e.getLocalizedMessage(),
                        Error.TAM_BOT_TOKEN_INCORRECT
                );
            }
            throw new ChatBotConstructorException(
                    "Can't subscribe bot with id="
                            + id
                            + " cause "
                            + e.getLocalizedMessage(),
                    Error.TAM_SERVICE_ERROR
            );
        }
    }

    public SuccessResponse disconnect(final String authToken, int id) {
        BotSchemeEntity bot = botSchemeService.getBotScheme(authToken, id);
        if (bot.getBotId() == null) {
            throw new ChatBotConstructorException(
                    "Can't unsubscribe bot with id="
                            + id
                            + " cause bot was not subscribed",
                    Error.TAM_BOT_UNSUBSCRIBED_ALREADY
            );
        }
        TamBotEntity tamBot = tamBotRepository
                .findById(new TamBotEntity.Id(bot.getBotId(), bot.getUserId()));
        TamTamBotAPI tamTamBotAPI = TamTamBotAPI.create(tamBot.getToken());
        try {
            SimpleQueryResult result = tamTamBotAPI
                    .unsubscribe(host + Endpoint.TAM_BOT + "/" + bot.getId())
                    .execute();
            if (result.isSuccess()) {
                bot.setBotId(null);
                transactionalUtils
                        .transactionalOperation(() -> {
                            tamBotRepository.deleteById(new TamBotEntity.Id(bot.getBotId(), bot.getUserId()));
                            botSchemaRepository.save(bot);
                        });
                return new SuccessResponseWrapper<>(tamBot);
            } else {
                throw new ChatBotConstructorException(
                        "Can't unsubscribe bot with id="
                                + id
                                + " cause success="
                                + result.isSuccess(),
                        Error.TAM_SERVICE_ERROR
                );
            }
        } catch (ClientException | APIException e) {
            throw new ChatBotConstructorException(
                    "Can't unsubscribe bot with id="
                            + id
                            + " cause "
                            + e.getLocalizedMessage(),
                    Error.TAM_SERVICE_ERROR
            );
        }
    }

    public SuccessResponse getUploadUrl(
            final String authToken,
            final int botSchemeId,
            final String attachmentType
    ) {
        if (StringUtils.isEmpty(attachmentType)) {
            throw new GenerateUploadLinkException(
                    "Attachment type is empty",
                    Error.ATTACHMENT_TYPE_EMPTY
            );
        }
        TamBotEntity tamBot = getTamBot(botSchemeService.getBotScheme(authToken, botSchemeId));
        TamTamBotAPI tamTamBotAPI = TamTamBotAPI.create(tamBot.getToken());
        try {
            UploadType uploadType = UploadType.create(attachmentType);
            UploadEndpoint uploadEndpoint = tamTamBotAPI.getUploadUrl(uploadType).execute();
            return new SuccessResponseWrapper<>(uploadEndpoint);
        } catch (IllegalArgumentException illegalArgumentException) {
            throw new GenerateUploadLinkException(
                    String.format("Attachment type is illegal %s", illegalArgumentException.getLocalizedMessage()),
                    Error.ATTACHMENT_TYPE_ILLEGAL
            );
        } catch (ClientException | APIException serviceException) {
            throw new GenerateUploadLinkException(
                    String.format("Service error %s", serviceException.getLocalizedMessage()),
                    Error.ATTACHMENT_UPLOAD_SERVICE_ERROR
            );
        }
    }
}
