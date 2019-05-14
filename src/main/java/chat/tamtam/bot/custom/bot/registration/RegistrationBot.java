package chat.tamtam.bot.custom.bot.registration;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;

import chat.tamtam.bot.converter.EnabledIds;
import chat.tamtam.bot.converter.EnabledIdsConverter;
import chat.tamtam.bot.custom.bot.AbstractCustomBot;
import chat.tamtam.bot.custom.bot.BotType;
import chat.tamtam.bot.domain.session.Session;
import chat.tamtam.bot.domain.user.UserEntity;
import chat.tamtam.bot.repository.SessionRepository;
import chat.tamtam.bot.repository.UserRepository;
import chat.tamtam.bot.security.SecurityConstants;
import chat.tamtam.botapi.exceptions.APIException;
import chat.tamtam.botapi.exceptions.ClientException;
import chat.tamtam.botapi.model.BotAddedToChatUpdate;
import chat.tamtam.botapi.model.BotRemovedFromChatUpdate;
import chat.tamtam.botapi.model.BotStartedUpdate;
import chat.tamtam.botapi.model.ChatTitleChangedUpdate;
import chat.tamtam.botapi.model.InlineKeyboardAttachmentRequest;
import chat.tamtam.botapi.model.InlineKeyboardAttachmentRequestPayload;
import chat.tamtam.botapi.model.Intent;
import chat.tamtam.botapi.model.LinkButton;
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
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

import static chat.tamtam.bot.security.SecurityConstants.EXPIRATION_TIME;
import static chat.tamtam.bot.security.SecurityConstants.SECRET;
import static chat.tamtam.bot.security.SecurityConstants.TOKEN_PREFIX;
import static com.auth0.jwt.algorithms.Algorithm.HMAC512;

@Log4j2
@Component
@RefreshScope
public class RegistrationBot extends AbstractCustomBot {
    private static final String HELP_MESSAGE =
            "Команды:\n\n"
                    + "/создать_аккаунт - создать аккаунт\n"
                    + "/удалить_аккаунт - удалить аккаунт\n"
                    + "/изменить_пароль - изменить пароль\n"
                    + "/войти_в_аккаунт - ссылка для входа";

    private final @NonNull UserRepository userRepository;
    private final @NonNull SessionRepository sessionRepository;
    private final @NonNull BCryptPasswordEncoder bCryptPasswordEncoder;

    private final EnabledIds enabledIds;

    private String url;

    private final RegistrationBotVisitor visitor;

    public RegistrationBot(
            @Value("${tamtam.bot.registration.id}") final String id,
            @Value("${tamtam.bot.registration.token}") final String token,
            @Value("${tamtam.host}") String host,
            @Value("${tamtam.bot.registration.enabledIds}") final String enabledIds,
            final UserRepository userRepository,
            final SessionRepository sessionRepository,
            final BCryptPasswordEncoder bCryptPasswordEncoder,
            final Environment environment,
            final EnabledIdsConverter enabledIdsConverter
    ) {
        super(id, token, host, environment);
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.enabledIds = enabledIdsConverter.convert(enabledIds, getClass());
        visitor = new RegistrationBotVisitor();
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
    public void process(final Update update) {
        log.info("Visit registration bot");
        try {
            update.visit(visitor);
        } catch (RuntimeException e) {
            log.error(String.format("Update event{%s} produced exception", update), e);
        }
    }

    private void response(final MessageCreatedUpdate update) {
        try {
            if (filter(update.getMessage().getSender().getUserId())) {
                NewMessageBody messageBody = resolve(update.getMessage());
                api.sendMessage(messageBody)
                        .userId(update.getMessage().getSender().getUserId())
                        .execute();
            }
        } catch (APIException | ClientException e) {
            log.error(
                    String.format(
                            "Bot(id=%s) can't response to event(type:%s, id:%s, sender:%d)", id,
                            update.getType(),
                            update.getMessage().getBody().getMid(),
                            update.getMessage().getSender().getUserId()
                    ),
                    e
            );
        }
    }

    private void response(final BotStartedUpdate update) {
        try {
            if (filter(update.getUserId())) {
                api.sendMessage(messageOf(HELP_MESSAGE))
                        .userId(update.getUserId())
                        .execute();
            }
        } catch (APIException | ClientException e) {
            log.error(
                    String.format(
                            "Bot(id=%s) can't response to event(type:%s, sender:%d)", id,
                            update.getType(),
                            update.getUserId()
                    ),
                    e
            );
        }
    }

    private boolean filter(final Long userId) {
        return enabledIds.isEnabled(userId);
    }

    private NewMessageBody resolve(final Message message) {
        String[] cmd = Optional
                .ofNullable(message.getBody().getText())
                .orElse("")
                .split(" ");
        switch (cmd[0]) {
            case "/создать_аккаунт":
                return registrate(message.getSender().getUserId().toString());
            case "/удалить_аккаунт":
                return delete(message.getSender().getUserId().toString());
            case "/изменить_пароль":
                if (cmd.length < 2) {
                    return messageOf("Отправьте пароль");
                } else {
                    return updatePassword(message.getSender().getUserId().toString(), cmd[1]);
                }
            case "/войти_в_аккаунт":
                return login(message.getSender().getUserId().toString());
            default:
                return messageOf(HELP_MESSAGE);
        }
    }

    private NewMessageBody login(final String userId) {
        UserEntity user = userRepository.findByLogin(userId);
        if (user != null) {
            return messageOf("Нажмите, чтобы войти в сервис", List.of(getAutoLoginButton(userId)));
        } else {
            return messageOf("Пользователь с id: " + userId + " не найден\nЧтобы создать аккаунт: /создать_аккаунт");
        }
    }

    private NewMessageBody registrate(final String userId) {
        UserEntity user = userRepository.findByLogin(userId);
        if (user == null) {
            // @todo #CC-36 Improve password generation
            String password = Math.random() + "_" + System.currentTimeMillis();
            user = new UserEntity(
                    userId,
                    bCryptPasswordEncoder.encode(password)
            );
            userRepository.save(user);
            String response = "Id пользователя: " + userId + "\nПароль: " + password;
            return messageOf(response, List.of(getAutoLoginButton(userId)));
        }
        String response = "Id пользователя: " + userId + "\nЧтобы изменить пароль: /изменить_пароль ";
        return messageOf(response, List.of(getAutoLoginButton(userId)));
    }

    private NewMessageBody delete(final String userId) {
        UserEntity user = userRepository.findByLogin(userId);
        if (user != null) {
            userRepository.removeByLogin(userId);
            sessionRepository.removeAllByLogin(userId);
            return messageOf("Пользователь с id: " + userId + " удален");
        }
        return messageOf("Пользователь с id: " + userId + " не найден\nЧтобы зарегистрироваться: /создать_аккаунт");
    }

    private NewMessageBody updatePassword(final String userId, final String newPassword) {
        UserEntity user = userRepository.findByLogin(userId);
        if (user != null) {
            if (newPassword.isEmpty()) {
                return messageOf("Попробуйте другой пароль");
            }
            user.setPasswordHash(bCryptPasswordEncoder.encode(newPassword));
            userRepository.save(user);
            String response = "Id пользователя: " + userId + "\nПароль: " + newPassword;
            sessionRepository.removeAllByLogin(userId);
            return messageOf(response, List.of(getAutoLoginButton(userId)));
        }
        return messageOf("Пользователь с id: " + userId
                + " не найден\nЧтобы зарегистрироваться: /создать_аккаунт");
    }

    private InlineKeyboardAttachmentRequest getAutoLoginButton(final String userId) {
        UserEntity user = userRepository.findByLogin(userId);
        String tempAccessToken = TOKEN_PREFIX
                + JWT.create()
                        .withExpiresAt(new Date())
                        .sign(HMAC512(SECRET.getBytes()));
        Session session =
                new Session(
                        tempAccessToken,
                        user.getId(),
                        user.getLogin(),
                        new Date(System.currentTimeMillis() + EXPIRATION_TIME)
                );
        sessionRepository.save(session);
        String tempAccessUrl = host + "/?" + SecurityConstants.AUTO_LOGIN_TEMP_ACCESS_TOKEN + "=" + tempAccessToken;
        LinkButton button = new LinkButton(tempAccessUrl, "войти", Intent.DEFAULT);
        return new InlineKeyboardAttachmentRequest(
                        new InlineKeyboardAttachmentRequestPayload(List.of(List.of(button))));
    }

    @Override
    public BotType getType() {
        return BotType.Registration;
    }

    private class RegistrationBotVisitor implements Update.Visitor {

        @Override
        public void visit(MessageCreatedUpdate model) {
            response(model);
        }

        @Override
        public void visit(MessageCallbackUpdate model) {

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
            response(model);
        }

        @Override
        public void visit(ChatTitleChangedUpdate model) {

        }

        @Override
        public void visitDefault(Update model) {

        }
    }
}
