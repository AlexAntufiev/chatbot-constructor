package chat.tamtam.bot.custom.bot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;

import chat.tamtam.bot.configuration.Profiles;
import chat.tamtam.bot.controller.Endpoints;
import chat.tamtam.bot.domain.SessionEntity;
import chat.tamtam.bot.domain.UserEntity;
import chat.tamtam.bot.repository.SessionRepository;
import chat.tamtam.bot.repository.UserRepository;
import chat.tamtam.bot.security.SecurityConstants;
import chat.tamtam.botapi.TamTamBotAPI;
import chat.tamtam.botapi.exceptions.APIException;
import chat.tamtam.botapi.exceptions.ClientException;
import chat.tamtam.botapi.model.InlineKeyboardAttachmentRequest;
import chat.tamtam.botapi.model.InlineKeyboardAttachmentRequestPayload;
import chat.tamtam.botapi.model.Intent;
import chat.tamtam.botapi.model.LinkButton;
import chat.tamtam.botapi.model.Message;
import chat.tamtam.botapi.model.NewMessageBody;
import chat.tamtam.botapi.model.SimpleQueryResult;
import chat.tamtam.botapi.model.SubscriptionRequestBody;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import static chat.tamtam.bot.security.SecurityConstants.EXPIRATION_TIME;
import static chat.tamtam.bot.security.SecurityConstants.SECRET;
import static chat.tamtam.bot.security.SecurityConstants.TOKEN_PREFIX;
import static com.auth0.jwt.algorithms.Algorithm.HMAC512;

@Component
@RequiredArgsConstructor
@Log4j2
public class RegistrationBot extends AbstractCustomBot {
    private static final String TAMTAM_HOST = "${tamtam.host}";
    private static final String TAMTAM_REGISTRATION_BOT_ID_PROP = "${tamtam.registration.bot.id}";
    private static final String TAMTAM_REGISTRATION_BOT_TOKEN_PROP = "${tamtam.registration.bot.token}";
    private static final String TAMTAM_REGISTRATION_BOT_ONLY_TRUSTED_USERS_PROP =
            "${tamtam.registration.bot.onlyTrustedUsers}";
    private static final String TAMTAM_REGISTRATION_BOT_TRUSTED_USERS_PROP = "${tamtam.registration.bot.trustedUsers}";

    private static final String HELP_MESSAGE =
            "Available commands:\n\n"
                    + "/reg - create user\n"
                    + "/del - delete user\n"
                    + "/upd {new password} - change password on specified one\n"
                    + "/login - receive auto-login button";

    private final @NonNull
    UserRepository userRepository;
    private final @NonNull
    SessionRepository sessionRepository;
    private final @NonNull
    Environment environment;
    private final @NonNull
    BCryptPasswordEncoder bCryptPasswordEncoder;
    private Set<String> trustedUsers;

    @Value(TAMTAM_REGISTRATION_BOT_ONLY_TRUSTED_USERS_PROP)
    private boolean onlyTrustedUsers = false;
    @Value(TAMTAM_REGISTRATION_BOT_ID_PROP)
    private String id;
    @Value(TAMTAM_REGISTRATION_BOT_TOKEN_PROP)
    private String token;
    @Value(TAMTAM_REGISTRATION_BOT_TRUSTED_USERS_PROP)
    private ArrayList<String> users;
    @Value(TAMTAM_HOST)
    private String host;

    private String url;
    private boolean subscribed = false;
    private TamTamBotAPI botAPI;

    @PostConstruct
    public void init() {
        botAPI = TamTamBotAPI.create(token);
        if (onlyTrustedUsers) {
            trustedUsers = new HashSet<>(users);
        } else {
            trustedUsers = null;
        }
    }

    @Override
    public void processMessage(final Message message) {
        try {
            if (!filter(message)) {
                return;
            }
            NewMessageBody messageBody = resolve(message);
            botAPI.sendMessage(messageBody)
                    .userId(message.getSender().getUserId())
                    .execute();
        } catch (APIException | ClientException | RuntimeException e) {
            log.error(String.format("Bot with id = [%s] can't process message", id), e.getMessage());
        }
    }

    private boolean filter(final Message message) {
        return !onlyTrustedUsers || trustedUsers.contains(message.getSender().getUserId().toString());
    }

    private NewMessageBody resolve(final Message message) {
        String[] cmd = message.getMessage().getText().split(" ");
        switch (cmd[0]) {
            case "/reg":
                return registrate(message.getSender().getUserId().toString());
            case "/del":
                return delete(message.getSender().getUserId().toString());
            case "/upd":
                if (cmd.length < 2) {
                    return new NewMessageBody("Please provide new password", null);
                } else {
                    return updatePassword(message.getSender().getUserId().toString(), cmd[1]);
                }
            case "/login":
                return login(message.getSender().getUserId().toString());
            default:
                return new NewMessageBody(HELP_MESSAGE, null);
        }
    }

    private NewMessageBody login(final String userId) {
        UserEntity user = userRepository.findByLogin(userId);
        if (user != null) {
            return new NewMessageBody("Press to sign in", List.of(getAutoLoginButton(userId)));
        } else {
            StringBuilder response = new StringBuilder()
                    .append("Login: ")
                    .append(userId)
                    .append(" not found\nTry '/reg' to create user");
            return new NewMessageBody(response.toString(), null);
        }
    }

    private NewMessageBody registrate(final String userId) {
        UserEntity user = userRepository.findByLogin(userId);
        if (user == null) {
            Long password = System.currentTimeMillis();
            user = new UserEntity(
                    userId,
                    bCryptPasswordEncoder.encode(password.toString())
            );
            userRepository.save(user);
            StringBuilder response = new StringBuilder()
                    .append("Login: ")
                    .append(userId)
                    .append("\nPassword: ")
                    .append(password);
            return new NewMessageBody(response.toString(), List.of(getAutoLoginButton(userId)));
        }
        StringBuilder response = new StringBuilder()
                .append("Login: ")
                .append(userId)
                .append("\nTry '/upd' to change password");
        return new NewMessageBody(response.toString(), List.of(getAutoLoginButton(userId)));
    }

    private NewMessageBody delete(final String userId) {
        UserEntity user = userRepository.findByLogin(userId);
        if (user != null) {
            userRepository.removeByLogin(userId);
            sessionRepository.removeAllByLogin(userId);
            StringBuilder response = new StringBuilder()
                    .append("User with login: ")
                    .append(userId)
                    .append(" deleted");
            return new NewMessageBody(response.toString(), null);
        }
        StringBuilder response = new StringBuilder()
                .append("User with login: ")
                .append(userId)
                .append(" not found\nTry '/reg' to create user");
        return new NewMessageBody(response.toString(), null);
    }

    private NewMessageBody updatePassword(final String userId, final String newPassword) {
        UserEntity user = userRepository.findByLogin(userId);
        if (user != null) {
            if (newPassword.isEmpty()) {
                return new NewMessageBody("Try another password", null);
            }
            user.setPasswordHash(bCryptPasswordEncoder.encode(newPassword));
            userRepository.save(user);
            StringBuilder response = new StringBuilder()
                    .append("Login: ")
                    .append(userId)
                    .append("\nPassword: ")
                    .append(newPassword);
            sessionRepository.removeAllByLogin(userId);
            return new NewMessageBody(response.toString(), List.of(getAutoLoginButton(userId)));
        }
        StringBuilder response = new StringBuilder()
                .append("Login: ")
                .append(userId)
                .append(" not found\nTry '/reg' to create user");
        return new NewMessageBody(response.toString(), null);
    }

    private InlineKeyboardAttachmentRequest getAutoLoginButton(final String userId) {
        UserEntity user = userRepository.findByLogin(userId);
        StringBuilder token = new StringBuilder()
                .append(TOKEN_PREFIX)
                .append(JWT.create()
                        .withExpiresAt(new Date(System.currentTimeMillis()))
                        .sign(HMAC512(SECRET.getBytes())));
        SessionEntity sessionEntity =
                new SessionEntity(
                        token.toString(),
                        user.getId(),
                        user.getLogin(),
                        new Date(System.currentTimeMillis() + EXPIRATION_TIME)
                );
        sessionRepository.save(sessionEntity);
        StringBuilder url = new StringBuilder()
                .append(host)
                .append("/?")
                .append(SecurityConstants.ACCESS_TOKEN_PARAM)
                .append("=")
                .append(token);
        LinkButton button = new LinkButton(url.toString(), "sign in", Intent.DEFAULT);
        InlineKeyboardAttachmentRequest inlineKeyboardAttachmentRequest =
                new InlineKeyboardAttachmentRequest(
                        new InlineKeyboardAttachmentRequestPayload(List.of(List.of(button))));
        return inlineKeyboardAttachmentRequest;
    }

    @Override
    public BotType getType() {
        return BotType.Registration;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getToken() {
        return token;
    }

    @Profile(Profiles.PRODUCTION)
    @Bean
    public void subscribeRegBotOnAppReadyProduction() {
        try {
            if (host == null) {
                throw new NullPointerException("Can't read host property");
            }
            url = host + Endpoints.TAM_BOT_WEBHOOK + "/" + id;
            SimpleQueryResult result = botAPI.subscribe(new SubscriptionRequestBody(url)).execute();
            if (!result.isSuccess()) {
                throw new IllegalStateException("Can't subscribe bot with id:" + id);
            }
            subscribed = true;
        } catch (NullPointerException | ClientException | APIException | IllegalStateException e) {
            log.error(String.format("Can't subscribe bot with id = [%s] via url = [%s]", id, url), e.getMessage());
        }
    }

    @Profile(Profiles.PRODUCTION)
    @PreDestroy
    public void unsubscribeRegBotOnAppShutdown() {
        if (!subscribed) {
            return;
        }
        try {
            if (url == null) {
                throw new NullPointerException("Url is null");
            }
            SimpleQueryResult result = botAPI.unsubscribe(url).execute();
            if (!result.isSuccess()) {
                throw new IllegalStateException("Can't unsubscribe bot with id:" + id);
            }
        } catch (ClientException | APIException | NullPointerException | IllegalStateException e) {
            log.error(String.format("Can't unsubscribe bot with id = [%s] via url = [%s]", id, url), e.getMessage());
        }
    }
}
