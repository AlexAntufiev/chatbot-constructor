package chat.tamtam.bot.custom.bot;

import java.util.Date;
import java.util.List;
import java.util.Optional;
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
import lombok.Getter;
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

    @Value("${tamtam.registration.bot.onlyTrustedUsers:false}")
    private boolean onlyTrustedUsers;
    @Getter
    @Value("${tamtam.registration.bot.id}")
    private String id;
    @Getter
    @Value("${tamtam.registration.bot.token}")
    private String token;
    @Value("${tamtam.registration.bot.trustedUsers:}#{T(java.util.Collections).emptySet()}")
    private Set<String> trustedUsers;
    @Value("${tamtam.host}")
    private String host;

    private String url;
    private boolean subscribed = false;
    private TamTamBotAPI botAPI;

    @PostConstruct
    public void init() {
        botAPI = TamTamBotAPI.create(token);
    }

    @Override
    public void processMessage(final Message message) {
        try {
            if (filter(message)) {
                NewMessageBody messageBody = resolve(message);
                botAPI.sendMessage(messageBody)
                        .userId(message.getSender().getUserId())
                        .execute();
            }
        } catch (APIException | ClientException e) {
            log.error(String.format("Bot with id = [%s] can't process message", id), e);
        }
    }

    private boolean filter(final Message message) {
        return !onlyTrustedUsers || trustedUsers.contains(message.getSender().getUserId().toString());
    }

    private NewMessageBody resolve(final Message message) {
        String[] cmd = Optional
                .ofNullable(message.getMessage().getText())
                .orElseGet(() -> "")
                .split(" ");
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
            String response = "Login: " + userId + " not found\nTry '/reg' to create user";
            return new NewMessageBody(response, null);
        }
    }

    private NewMessageBody registrate(final String userId) {
        UserEntity user = userRepository.findByLogin(userId);
        if (user == null) {
            //@todo #CC-36 Improve password generation
            String password = Math.random() + "_" + System.currentTimeMillis();
            user = new UserEntity(
                    userId,
                    bCryptPasswordEncoder.encode(password)
            );
            userRepository.save(user);
            String response = "Login: " + userId + "\nPassword: " + password;
            return new NewMessageBody(response, List.of(getAutoLoginButton(userId)));
        }
        String response = "Login: " + userId + "\nTry '/upd' to change password";
        return new NewMessageBody(response, List.of(getAutoLoginButton(userId)));
    }

    private NewMessageBody delete(final String userId) {
        UserEntity user = userRepository.findByLogin(userId);
        if (user != null) {
            userRepository.removeByLogin(userId);
            sessionRepository.removeAllByLogin(userId);
            String response = "User with login: " + userId + " deleted";
            return new NewMessageBody(response, null);
        }
        String response = "User with login: " + userId + " not found\nTry '/reg' to create user";
        return new NewMessageBody(response, null);
    }

    private NewMessageBody updatePassword(final String userId, final String newPassword) {
        UserEntity user = userRepository.findByLogin(userId);
        if (user != null) {
            if (newPassword.isEmpty()) {
                return new NewMessageBody("Try another password", null);
            }
            user.setPasswordHash(bCryptPasswordEncoder.encode(newPassword));
            userRepository.save(user);
            String response = "Login: " + userId + "\nPassword: " + newPassword;
            sessionRepository.removeAllByLogin(userId);
            return new NewMessageBody(response, List.of(getAutoLoginButton(userId)));
        }
        String response = "Login: " + userId + " not found\nTry '/reg' to create user";
        return new NewMessageBody(response, null);
    }

    private InlineKeyboardAttachmentRequest getAutoLoginButton(final String userId) {
        UserEntity user = userRepository.findByLogin(userId);
        String tempAccessToken = TOKEN_PREFIX
                + JWT.create()
                        .withExpiresAt(new Date())
                        .sign(HMAC512(SECRET.getBytes()));
        SessionEntity sessionEntity =
                new SessionEntity(
                        tempAccessToken,
                        user.getId(),
                        user.getLogin(),
                        new Date(System.currentTimeMillis() + EXPIRATION_TIME)
                );
        sessionRepository.save(sessionEntity);
        String tempAccessUrl = host + "/?" + SecurityConstants.AUTO_LOGIN_TEMP_ACCESS_TOKEN + "=" + tempAccessToken;
        LinkButton button = new LinkButton(tempAccessUrl, "sign in", Intent.DEFAULT);
        return new InlineKeyboardAttachmentRequest(
                        new InlineKeyboardAttachmentRequestPayload(List.of(List.of(button))));
    }

    @Override
    public BotType getType() {
        return BotType.Registration;
    }

    @Profile(Profiles.PRODUCTION)
    @Bean
    public void subscribeRegBotOnAppReadyProduction() {
        try {
            url = host + Endpoints.TAM_BOT_WEBHOOK + "/" + id;
            SimpleQueryResult result = botAPI.subscribe(new SubscriptionRequestBody(url)).execute();
            if (!result.isSuccess()) {
                log.debug("Can't subscribe bot with id:" + id);
            }
            subscribed = true;
        } catch (ClientException | APIException e) {
            log.error(String.format("Can't subscribe bot with id = [%s] via url = [%s]", id, url), e);
        }
    }

    @Profile(Profiles.PRODUCTION)
    @PreDestroy
    public void unsubscribeRegBotOnAppShutdown() {
        if (!subscribed) {
            return;
        }
        try {
            SimpleQueryResult result = botAPI.unsubscribe(url).execute();
            if (!result.isSuccess()) {
                log.debug("Can't unsubscribe bot with id:" + id);
            }
        } catch (ClientException | APIException e) {
            log.error(String.format("Can't unsubscribe bot with id = [%s] via url = [%s]", id, url), e);
        }
    }
}
