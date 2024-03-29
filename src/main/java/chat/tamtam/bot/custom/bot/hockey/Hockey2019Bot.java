package chat.tamtam.bot.custom.bot.hockey;

import chat.tamtam.bot.custom.bot.AbstractCustomBot;
import chat.tamtam.bot.custom.bot.BotType;
import chat.tamtam.bot.domain.bot.hockey.Calendar;
import chat.tamtam.bot.domain.bot.hockey.Match;
import chat.tamtam.bot.domain.bot.hockey.Team;
import chat.tamtam.bot.service.hockey.Hockey2019Service;
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
import chat.tamtam.botapi.model.Update;
import chat.tamtam.botapi.model.UserAddedToChatUpdate;
import chat.tamtam.botapi.model.UserRemovedFromChatUpdate;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j2
@Component
@ConditionalOnProperty(
        prefix = "tamtam.bot.hockey2019",
        name = "enabled",
        havingValue = "true"
)
public class Hockey2019Bot extends AbstractCustomBot {

    private static final String HELLO_MESSAGE = "Чемпионат мира по хоккею 2019";
    private static final String INFO = "Инфо";
    private static final String NEWS = "Новости";
    private static final String TEAM_NEWS = "Новости_команд";
    private static final String SELECTED_TEAM_NEWS = "Команды";
    private static final String CALENDAR = "Календарь";
    private static final String RESULTS = "Результаты";
    private static final String MATCH = "Онлайн-трансляция";
    private static final String SELECTED_MATCH = "Матчи";

    private static final NewMessageBody INFO_MESSAGE;
    private static final NewMessageBody HELP_MESSAGE_BUTTONS;
    private static final NewMessageBody TEAMS;

    static {
        // CHECKSTYLE_OFF: ALMOST_ALL
        INFO_MESSAGE = messageOf("83-ий Чемпионат мира\uD83C\uDFC6\uD83C\uDF0D по хоккею с шайбой\uD83C\uDFD2 "
                + "проходит в Словакии\uD83C\uDDF8\uD83C\uDDF0 в городах Братислава и Кошица"
                + " с 10 по 26 мая 2019\uD83D\uDCC5.\n"
                + "В этом году чемпионский титул защищает сборная Швеции\uD83C\uDDF8\uD83C\uDDEA.\n"
                + "Впервые с 1994 года на Чемпионате мира по хоккею с шайбой "
                + "выступит сборная Великобритании\uD83C\uDDEC\uD83C\uDDE7.");
        // CHECKSTYLE_OFF: ALMOST_ALL

        HELP_MESSAGE_BUTTONS = messageOf(
                HELLO_MESSAGE,
                List.of(
                        new InlineKeyboardAttachmentRequest(new InlineKeyboardAttachmentRequestPayload(
                                Stream.of(INFO, NEWS, TEAM_NEWS, CALENDAR, RESULTS, MATCH)
                                .map(s -> {
                                    Button button = new CallbackButton(s, s, Intent.POSITIVE);
                                    return button;
                                })
                                .map(List::of)
                                .collect(Collectors.toList())
                        ))
                )
        );

        TEAMS = messageOf("Выбери команду",
                List.of(new InlineKeyboardAttachmentRequest(new InlineKeyboardAttachmentRequestPayload(
                        Stream.of(Team.values())
                        .map(team -> new ArrayList<Button>() {{
                            add(new CallbackButton(
                                    SELECTED_TEAM_NEWS + " " + team.getId(), team.getName(), Intent.POSITIVE
                            ));
                        }})
                        .collect(Collectors.toList()))))
        );
    }

    private final Hockey2019Service hockey2019Service;

    private final Hockey2019BotVisitor hockey2019BotVisitor;

    public Hockey2019Bot(
            @Value("${tamtam.bot.hockey2019.id}") final String id,
            @Value("${tamtam.bot.hockey2019.token}") final String token,
            @Value("${tamtam.host}") final String host,
            final Hockey2019Service hockey2019Service,
            final Environment environment
    ) {
        super(id, token, host, environment);
        this.hockey2019Service = hockey2019Service;
        hockey2019BotVisitor = new Hockey2019BotVisitor();
    }

    @Override
    public BotType getType() {
        return BotType.Hockey2019;
    }

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

        sendMessage(
                updateMessage.getSender().getUserId(),
                updateMessage.getRecipient().getChatId(),
                HELP_MESSAGE_BUTTONS
        );
    }

    private void callbackUpdate(MessageCallbackUpdate update) {
        Callback callback = update.getCallback();
        Message message = update.getMessage();
        Long userId = message.getSender().getUserId();
        Long targetUserId = message.getRecipient().getUserId();

        String[] payload = callback.getPayload().split(" ");

        switch (payload[0]) {
            case INFO:
                log.info("HOCKEY_BOT_2019 INFO {}", targetUserId);
                sendCallbackMessage(callback, message, INFO_MESSAGE);
                break;
            case NEWS:
                log.info("HOCKEY_BOT_2019 NEWS {}", targetUserId);
                sendCallbackMessage(callback, message, news());
                break;
            case TEAM_NEWS:
                log.info("HOCKEY_BOT_2019 TEAM_NEWS {}", targetUserId);
                sendCallbackMessage(callback, message, TEAMS);
                return;
            case SELECTED_TEAM_NEWS:
                log.info(
                        "HOCKEY_BOT_2019 SELECTED_TEAM_NEWS {} {}",
                        targetUserId,
                        Team.getById(Integer.parseInt(payload[1])).getName()
                );
                sendCallbackMessage(callback, message, news(Integer.parseInt(payload[1])));
                break;
            case CALENDAR:
                log.info("HOCKEY_BOT_2019 CALENDAR {}", targetUserId);
                sendCallbackMessage(callback, message, calendar());
                break;
            case RESULTS:
                log.info("HOCKEY_BOT_2019 RESULTS {}", targetUserId);
                sendCallbackMessage(callback, message, results());
                break;
            case MATCH:
                log.info("HOCKEY_BOT_2019 MATCH {}", targetUserId);
                NewMessageBody matches = matches();
                if(matches == null){
                    sendCallbackMessage(callback, message, messageOf("Нет активных матчей."));
                    break;
                }
                sendCallbackMessage(callback, message, matches);
                return;
            case SELECTED_MATCH:
                log.info("HOCKEY_BOT_2019 SELECTED_MATCH {}", targetUserId);
                Match match = match(Integer.parseInt(payload[1]));

                sendCallbackMessage(callback, message, messageOf(match.getMatchInfo()));

                match.getMessages()
                        .map(AbstractCustomBot::messageOf)
                        .forEach(newMessageBody -> sendMessage(
                            userId,
                            message.getRecipient().getChatId(),
                            newMessageBody
                        )
                );
                break;
        }
        sendMessage(userId, message.getRecipient().getChatId(), HELP_MESSAGE_BUTTONS);
    }

    private Match match(int matchId) {
        return hockey2019Service
                .getMatch(matchId);
    }

    private NewMessageBody results() {
        return messageOf(hockey2019Service
                .getResults()
                .getMessages());
    }

    private NewMessageBody calendar() {
        return messageOf(hockey2019Service
                .getCalendar()
                .getMessages());
    }

    private NewMessageBody matches() {

        List<Calendar.Entity> availableMatches = hockey2019Service.getCalendar().getAvailableMatches();
        if(availableMatches.isEmpty()){
            return null;
        }

        return messageOf("Выбери матч",
                List.of(new InlineKeyboardAttachmentRequest(new InlineKeyboardAttachmentRequestPayload(
                        availableMatches.stream()
                                .map(entity -> new ArrayList<Button>() {{
                                    add(new CallbackButton(SELECTED_MATCH + " " + entity.getId(),
                                            entity.getMatchInfo(),
                                            Intent.POSITIVE
                                    ));
                                }})
                                .collect(Collectors.toList())))));
    }

    private NewMessageBody news() {
        return messageOf(hockey2019Service
                .getNews()
                .getMessages());
    }

    private NewMessageBody news(int teamId) {
        return messageOf(hockey2019Service
                .getNewsOfTeam(teamId)
                .getMessages());
    }

    private class Hockey2019BotVisitor implements Update.Visitor {
        @Override
        public void visit(MessageCreatedUpdate model) {
            log.info("HOCKEY_BOT_2019 MESSAGE {}", model.getMessage().getRecipient().getUserId());
            createdUpdate(model);
        }

        @Override
        public void visit(MessageCallbackUpdate model) {
            log.info("HOCKEY_BOT_2019 CALLBACK {}", model.getMessage().getRecipient().getUserId());
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
            log.info("HOCKEY_BOT_2019 START_MESSAGE {}", model.getUserId());
            sendMessage(model.getUserId(), model.getChatId(), HELP_MESSAGE_BUTTONS);
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
