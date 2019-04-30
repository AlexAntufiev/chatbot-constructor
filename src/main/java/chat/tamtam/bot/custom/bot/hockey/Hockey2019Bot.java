package chat.tamtam.bot.custom.bot.hockey;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import chat.tamtam.bot.configuration.Profiles;
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
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
@RequiredArgsConstructor
public class Hockey2019Bot extends AbstractCustomBot {

    private static final String HELLO_MESSAGE = "Чемпионат мира по хоккею 2019";
    private static final String NEWS = "/новости";
    private static final String TEAM_NEWS = "/новости_команды";
    private static final String CALENDAR = "/календарь";
    private static final String RESULTS = "/результаты";
    private static final String MATCH = "/матч";

    private static final String HELP_MESSAGE = String.format(
            "%s:\n\n%s\n%s\n%s\n%s\n%s\n",
            HELLO_MESSAGE,
            NEWS,
            TEAM_NEWS,
            CALENDAR,
            RESULTS,
            MATCH
    );

    private final @NonNull Hockey2019Service hockey2019Service;

    @Getter
    @Value("${tamtam.bot.hockey2019.id}")
    private String id;

    @Value("${tamtam.bot.hockey2019.token}")
    private String token;

    @Value("${tamtam.host}")
    private String host;

    private String url;
    private boolean subscribed = false;
    private TamTamBotAPI botAPI;

    private Hockey2019BotVisitor hockey2019BotVisitor;

    @PostConstruct
    public void init() {
        botAPI = TamTamBotAPI.create(token);
        hockey2019BotVisitor = new Hockey2019BotVisitor();
        url = host + Endpoint.TAM_CUSTOM_BOT_WEBHOOK + "/" + id;
        log.info(String.format("Hockey 2019 bot(id:%s, token:%s) initialized", id, token));
    }

    @Loggable
    @Override
    public void process(final Update update) {
        update.visit(hockey2019BotVisitor);
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
        String[] cmd = Optional
                .ofNullable(message.getBody().getText())
                .orElse("")
                .split(" ");
        switch (cmd[0]) {
            case NEWS:
                return news();
            case TEAM_NEWS:
                return selectTeam(message);
            case CALENDAR:
                return calendar();
            case RESULTS:
                return results();
            case MATCH:
                // @todo ##CC-173 implement match of id function
                return match(1);
            default:
                return Stream.of(messageOf(HELP_MESSAGE));
        }
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

    private static Stream<NewMessageBody> selectTeam(Message message) {

        return Stream.of(messageOf(
                "Выбери команду",
                List.of(
                        new InlineKeyboardAttachmentRequest(new InlineKeyboardAttachmentRequestPayload(
                                Stream.of(Team.values())
                                .map(team -> new ArrayList<Button>() {{
                                    String teamName = team.getName();
                                    add(new CallbackButton(teamName, teamName, Intent.DEFAULT));
                                }})
                                .collect(Collectors.toList()))))
        ));
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

    @Profile({Profiles.PRODUCTION, Profiles.TEST, Profiles.DEVELOPMENT})
    @Bean
    public void subscribeRegBotOnAppReadyProduction() {
        try {
            SimpleQueryResult result = botAPI.subscribe(new SubscriptionRequestBody(url)).execute();
            if (result.isSuccess()) {
                log.info(String.format("Hockey 2019 bot(id:%s, token:%s) subscribed on %s", id, token, url
                ));
            } else {
                log.warn(String.format("Can't subscribe Hockey 2019 bot(id:%s, token:%s) on %s", id, token, url
                ));
            }
            //FIXME delete flag
            subscribed = true;
        } catch (ClientException | APIException e) {
            log.error(String.format("Can't subscribe bot with id = [%s] via url = [%s]", id, url), e);
        }
    }

    @Profile({Profiles.PRODUCTION, Profiles.TEST, Profiles.DEVELOPMENT})
    @PreDestroy //FIXME вызывается в любом случае
    public void unsubscribeRegBotOnAppShutdown() {
        if (!subscribed) {
            return;
        }
        try {
            SimpleQueryResult result = botAPI.unsubscribe(url).execute();
            log.info(String.format("Hockey 2019 bot(id:%s, token:%s) unsubscribed from %s", id, token, url));
            if (!result.isSuccess()) {
                log.warn(String.format("Can't unsubscribe Hockey 2019 bot(id:%s, token:%s) on %s", id, token, url
                ));
            }
        } catch (ClientException | APIException e) {
            log.error(String.format("Can't unsubscribe bot with id = [%s] via url = [%s]", id, url), e);
        }
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

    @Override
    public BotType getType() {
        return BotType.Hockey2019;
    }

    private void sendMessage(Long userId, Long chatId,  NewMessageBody newMessage) {
        try {
            botAPI.sendMessage(newMessage).userId(userId).chatId(chatId).execute();
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
            botAPI.answerOnCallback(
                    new CallbackAnswer()
                            .userId(userId)
                            .message(newMessage),
                    callback.getCallbackId())
                    .execute();
        } catch (ClientException | APIException e) {
            log.error(
                    String.format(
                        "Bot(id=%s) can't send messageOf to event(id:%s, sender:%d)",
                        id,
                        message.getBody().getMid(),
                        userId
                    ),
                    e
            );
        }
    }
}
