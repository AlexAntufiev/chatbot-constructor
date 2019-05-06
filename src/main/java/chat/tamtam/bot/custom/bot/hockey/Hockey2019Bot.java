package chat.tamtam.bot.custom.bot.hockey;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import chat.tamtam.bot.configuration.AppProfiles;
import chat.tamtam.bot.configuration.logging.Loggable;
import chat.tamtam.bot.controller.Endpoint;
import chat.tamtam.bot.custom.bot.AbstractCustomBot;
import chat.tamtam.bot.custom.bot.BotType;
import chat.tamtam.bot.domain.bot.hockey.Team;
import chat.tamtam.bot.service.hockey.Hockey2019Service;
import chat.tamtam.botapi.TamTamBotAPI;
import chat.tamtam.botapi.exceptions.APIException;
import chat.tamtam.botapi.exceptions.ClientException;
import chat.tamtam.botapi.model.BotAddedToChatUpdate;
import chat.tamtam.botapi.model.BotRemovedFromChatUpdate;
import chat.tamtam.botapi.model.BotStartedUpdate;
import chat.tamtam.botapi.model.Button;
import chat.tamtam.botapi.model.Callback;
import chat.tamtam.botapi.model.CallbackAnswer;
import chat.tamtam.botapi.model.CallbackButton;
import chat.tamtam.botapi.model.ChatTitleChangedUpdate;
import chat.tamtam.botapi.model.InlineKeyboardAttachmentRequest;
import chat.tamtam.botapi.model.InlineKeyboardAttachmentRequestPayload;
import chat.tamtam.botapi.model.Intent;
import chat.tamtam.botapi.model.Message;
import chat.tamtam.botapi.model.MessageCallbackUpdate;
import chat.tamtam.botapi.model.MessageCreatedUpdate;
import chat.tamtam.botapi.model.MessageEditedUpdate;
import chat.tamtam.botapi.model.MessageRemovedUpdate;
import chat.tamtam.botapi.model.MessageRestoredUpdate;
import chat.tamtam.botapi.model.NewMessageBody;
import chat.tamtam.botapi.model.SimpleQueryResult;
import chat.tamtam.botapi.model.SubscriptionRequestBody;
import chat.tamtam.botapi.model.Update;
import chat.tamtam.botapi.model.UserAddedToChatUpdate;
import chat.tamtam.botapi.model.UserRemovedFromChatUpdate;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
@RefreshScope
public class Hockey2019Bot extends AbstractCustomBot {

    // CHECKSTYLE_OFF: ALMOST_ALL
    private static final String INFO_MESSAGE =
            "Чемпионат мира\uD83C\uDFC6\uD83C\uDF0D по хоккею\uD83C\uDFD2 стартует в 83 раз.\n"
            + "Местом его проведения была выбрана Словакия\uD83C\uDDF8\uD83C\uDDF0 (города Братислава и Кошица).\n"
            + "Соревнование начнется 10 мая и продлиться до 26 мая\uD83D\uDCC5.\n"
            + "В этом году чемпионский титул будет защищать сборная Швеции\uD83C\uDDF8\uD83C\uDDEA.\n"
            + "Впервые с 1994 года на чемпионате мира выступит сборная Великобритании\uD83C\uDDEC\uD83C\uDDE7.";
    // CHECKSTYLE_OFF: ALMOST_ALL
    private static final String HELLO_MESSAGE = "Чемпионат мира по хоккею 2019";
    private static final String INFO = "/инфо";
    private static final String NEWS = "/новости";
    private static final String TEAM_NEWS = "/новости_команды";
    private static final String CALENDAR = "/календарь";
    private static final String RESULTS = "/результаты";
    private static final String MATCH = "/матч";

    private static final String HELP_MESSAGE = String.format(
            "%s:\n\n%s\n%s\n%s\n%s\n%s\n%s\n",
            HELLO_MESSAGE,
            INFO,
            NEWS,
            TEAM_NEWS,
            CALENDAR,
            RESULTS,
            MATCH
    );
    private static final Stream<NewMessageBody> TEAMS;
    private static final NewMessageBody HELP_MESSAGE_BODY;

    static {
        TEAMS = Stream.of(messageOf(
                "Выбери команду",
                List.of(new InlineKeyboardAttachmentRequest(new InlineKeyboardAttachmentRequestPayload(
                        Stream.of(Team.values())
                                .map(team -> new ArrayList<Button>() {{
                                    add(new CallbackButton(team.getName(), team.getName(), Intent.DEFAULT));
                                }})
                                .collect(Collectors.toList()))))
        ));
        HELP_MESSAGE_BODY = messageOf(HELP_MESSAGE);
    }

    private final Hockey2019Service hockey2019Service;
    private final Environment environment;

    private String url;
    private TamTamBotAPI api;

    private Hockey2019BotVisitor hockey2019BotVisitor;

    public Hockey2019Bot(
            @Value("${tamtam.bot.hockey2019.id}") final String id,
            @Value("${tamtam.bot.hockey2019.token}") final String token,
            @Value("${tamtam.host}") final String host,
            final Hockey2019Service hockey2019Service,
            final Environment environment
    ) {
        super(id, token, host);
        this.hockey2019Service = hockey2019Service;
        this.environment = environment;
        subscribe();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public BotType getType() {
        return BotType.Hockey2019;
    }

    public final void subscribe() {
        api = TamTamBotAPI.create(token);
        hockey2019BotVisitor = new Hockey2019BotVisitor();
        url = host + Endpoint.TAM_CUSTOM_BOT_WEBHOOK + "/" + id;
        log.info(String.format("Hockey 2019 bot(id:%s, token:%s) initialized", id, token));
        if (environment.acceptsProfiles(AppProfiles.noDevelopmentProfiles())) {
            try {
                SimpleQueryResult result = api.subscribe(new SubscriptionRequestBody(url)).execute();
                if (result.isSuccess()) {
                    log.info(String.format("Hockey 2019 bot(id:%s, token:%s) subscribed on %s", id, token, url));
                } else {
                    log.warn(String.format("Can't subscribe Hockey 2019 bot(id:%s, token:%s) on %s", id, token, url));
                }
            } catch (ClientException | APIException e) {
                log.error(String.format("Can't subscribe bot with id = [%s] via url = [%s]", id, url), e);
            }
        }
    }

    @PreDestroy
    public final void unsubscribe() {
        if (environment.acceptsProfiles(AppProfiles.noDevelopmentProfiles())) {
            try {
                SimpleQueryResult result = api.unsubscribe(url).execute();
                log.info(String.format("Hockey 2019 bot(id:%s, token:%s) unsubscribed from %s", id, token, url));
                if (!result.isSuccess()) {
                    log.warn(String.format("Can't unsubscribe Hockey 2019 bot(id:%s, token:%s) on %s", id, token, url));
                }
            } catch (ClientException | APIException e) {
                log.error(String.format("Can't unsubscribe bot with id = [%s] via url = [%s]", id, url), e);
            }
        }
    }

    @Loggable
    @Override
    public void process(final Update update) {
        log.info("Visit hockey bot 2019");
        try {
            update.visit(hockey2019BotVisitor);
        } catch (RuntimeException e) {
            log.error(String.format("Update event{%s} produced exception", update), e);
        }
    }

    private void createdUpdate(final MessageCreatedUpdate update) {

        Message updateMessage = update.getMessage();

        resolve(updateMessage)
                .forEach(newMessage -> sendMessage(
                        updateMessage.getSender().getUserId(),
                        updateMessage.getRecipient().getChatId(),
                        newMessage)
                );
    }

    private Stream<NewMessageBody> resolve(final Message message) {
        switch (message.getBody().getText()) {
            case INFO:
                return info();
            case NEWS:
                return news();
            case TEAM_NEWS:
                return TEAMS;
            case CALENDAR:
                return calendar();
            case RESULTS:
                return results();
            case MATCH:
                // @todo ##CC-173 implement match of id function
                return match(1);
            default:
                return Stream.of(HELP_MESSAGE_BODY);
        }
    }

    private static Stream<NewMessageBody> info() {
        return Stream.of(messageOf(INFO_MESSAGE));
    }

    private void callbackUpdate(MessageCallbackUpdate update) {
        Callback callback = update.getCallback();
        Message message = update.getMessage();
        Long userId = message.getSender().getUserId();

        String teamName = callback.getPayload();

        sendCallbackMessage(callback, message, messageOf(String.format("Новости команды: %s", teamName)));

        news(teamName).forEach(newMessageBody -> sendMessage(
                message.getSender().getUserId(),
                message.getRecipient().getChatId(),
                newMessageBody)
        );
    }

    private Stream<NewMessageBody> match(int matchId) {
        return Stream.of(messageOf("Скоро будет сделано"));
//        return hockey2019Service.getMatch(matchId).getMessages();
    }

    private Stream<NewMessageBody> results() {
        return hockey2019Service
                .getResults()
                .getMessages()
                .map(AbstractCustomBot::messageOf);
    }

    private Stream<NewMessageBody> calendar() {
        return hockey2019Service
                .getCalendar()
                .getMessages()
                .map(AbstractCustomBot::messageOf);
    }

    private Stream<NewMessageBody> news() {
        return hockey2019Service
                .getNews()
                .getMessages()
                .map(AbstractCustomBot::messageOf);
    }

    private Stream<NewMessageBody> news(String teamName) {
        return hockey2019Service
                .getNewsOfTeam(Team.getIdByName(teamName))
                .getMessages()
                .map(AbstractCustomBot::messageOf);
    }

    private class Hockey2019BotVisitor implements Update.Visitor {
        @Override
        public void visit(MessageCreatedUpdate model) {
            createdUpdate(model);
        }

        @Override
        public void visit(MessageCallbackUpdate model) {
            callbackUpdate(model);
        }

        @Override
        public void visit(MessageEditedUpdate model) {

        }

        @Override
        public void visit(MessageRemovedUpdate model) {

        }

        @Override
        public void visit(MessageRestoredUpdate model) {

        }

        @Override
        public void visit(BotAddedToChatUpdate model) {

        }

        @Override
        public void visit(BotRemovedFromChatUpdate model) {

        }

        @Override
        public void visit(UserAddedToChatUpdate model) {

        }

        @Override
        public void visit(UserRemovedFromChatUpdate model) {

        }

        @Override
        public void visit(BotStartedUpdate model) {
            sendMessage(model.getUserId(), model.getChatId(), messageOf(HELP_MESSAGE));
        }

        @Override
        public void visit(ChatTitleChangedUpdate model) {

        }

        @Override
        public void visitDefault(Update model) {

        }

    }

    private void sendMessage(Long userId, Long chatId,  NewMessageBody newMessage) {
        try {
            api.sendMessage(newMessage).userId(userId).chatId(chatId).execute();
        } catch (APIException | ClientException e) {
            log.error(
                    String.format("Bot(id=%s) can't send message to user (id:%s) to chat (id:%s)", id, userId, chatId),
                    e
            );
        }
    }

    private void sendCallbackMessage(Callback callback, Message message, NewMessageBody newMessage) {
        Long userId = message.getSender().getUserId();

        try {
            api.answerOnCallback(
                    new CallbackAnswer()
                            .userId(userId)
                            .message(newMessage),
                    callback.getCallbackId())
                    .execute();
        } catch (ClientException | APIException e) {
            log.error(
                    String.format(
                        "Bot(id=%s) can't send message to event(id:%s, sender:%d)",
                        id,
                        message.getBody().getMid(),
                        userId
                    ),
                    e
            );
        }
    }
}
