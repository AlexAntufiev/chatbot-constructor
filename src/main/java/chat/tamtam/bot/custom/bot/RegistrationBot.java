package chat.tamtam.bot.custom.bot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import chat.tamtam.bot.configuration.Profiles;
import chat.tamtam.bot.domain.UserEntity;
import chat.tamtam.bot.domain.webhook.WebHookMessageEntity;
import chat.tamtam.bot.repository.UserRepository;
import chat.tamtam.botapi.TamTamBotAPI;
import chat.tamtam.botapi.exceptions.APIException;
import chat.tamtam.botapi.exceptions.ClientException;
import chat.tamtam.botapi.model.NewMessageBody;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Component
@RequiredArgsConstructor
@Log4j2
public class RegistrationBot extends AbstractCustomBot {
    private static final String TAMTAM_REGISTRATION_BOT_ID_PROP = "tamtam.registration.bot.id";
    private static final String TAMTAM_REGISTRATION_BOT_TOKEN_PROP = "tamtam.registration.bot.token";
    private static final String TAMTAM_REGISTRATION_BOT_TRUSTED_USERS_PROP = "tamtam.registration.bot.trustedUsers";

    private static final String HELP_MESSAGE =
            "Available commands:\n\n"
                    + "/reg - create user\n"
                    + "/del - delete user\n"
                    + "/upd {new password} - change password on specified one\n";

    private Set<String> trustedUsers;

    private String id;
    private String token;

    private final @NonNull UserRepository userRepository;
    private final @NonNull Environment environment;

    private TamTamBotAPI botAPI;

    private final @NonNull BCryptPasswordEncoder bCryptPasswordEncoder;

    @PostConstruct
    public void init() {
        id = environment.getProperty(TAMTAM_REGISTRATION_BOT_ID_PROP);
        token = environment.getProperty(TAMTAM_REGISTRATION_BOT_TOKEN_PROP);
        botAPI = TamTamBotAPI.create(token);
        ArrayList<String> users =
                environment.getProperty(TAMTAM_REGISTRATION_BOT_TRUSTED_USERS_PROP, ArrayList.class);
        trustedUsers = new HashSet<>(users);
    }

    @Override
    public void processMessage(final WebHookMessageEntity message) {
        try {
            NewMessageBody messageBody = resolve(message);
            botAPI.sendMessage(messageBody)
                    .userId(message.getSender().getUserId())
                    .execute();
        } catch (APIException | ClientException | RuntimeException e) {
            log.error("Registration bot processMessage {}", e.getMessage());
        }
    }

    private boolean doFilter(final WebHookMessageEntity message) {
        return trustedUsers.contains(message.getSender().getUserId().toString());
    }

    private NewMessageBody resolve(final WebHookMessageEntity message) {
        String response = null;
        UserEntity user = null;
        String[] cmd = message.getMessage().getText().split(" ");
        switch (cmd[0]) {
            case "/reg":
                return registrate(message.getSender().getUserId().toString());
            case "/del":
                return delete(message.getSender().getUserId().toString());
            case "/upd":
                if (cmd.length < 2) {
                    response = "Please provide new password";
                    return new NewMessageBody(response, null);
                } else {
                    return updatePassword(message.getSender().getUserId().toString(), cmd[1]);
                }
            default:
                return new NewMessageBody(HELP_MESSAGE, null);
        }
    }

    private NewMessageBody registrate(final String userId) {
        String response;
        UserEntity user = userRepository.findByLogin(userId);
        if (user == null) {
            Long password = System.currentTimeMillis();
            user = new UserEntity(
                    userId,
                    bCryptPasswordEncoder.encode(password.toString())
            );
            userRepository.save(user);
            response = "Login: " + user.getLogin() + "\nPassword: " + password;
        } else {
            response = "Login: " + user.getLogin() + "\nTry '/upd' to change password";
        }
        return new NewMessageBody(response, null);
    }

    private NewMessageBody delete(final String userId) {
        String response;
        UserEntity user = userRepository.findByLogin(userId);
        if (user != null) {
            userRepository.removeByLogin(userId);
            response = "User with login: " + userId + " deleted";
        } else {
            response = "Login: " + userId + " not found\nTry '/reg' to create user";
        }
        return new NewMessageBody(response, null);
    }

    private NewMessageBody updatePassword(final String userId, final String newPassword) {
        String response;
        UserEntity user = userRepository.findByLogin(userId);
        if (user != null) {
            if (newPassword.isEmpty()) {
                response = "Try another password";
                return new NewMessageBody(response, null);
            }
            user.setPasswordHash(bCryptPasswordEncoder.encode(newPassword));
            userRepository.save(user);
            response = "Login: " + user.getLogin() + "\nPassword: " + newPassword;
        } else {
            response = "Login: " + userId + " not found\nTry '/reg' to create user";
        }
        return new NewMessageBody(response, null);
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

    @EventListener(ApplicationReadyEvent.class)
    @Profile(Profiles.PRODUCTION)
    public void subscribeRegBotOnAppReadyProduction() {
    }

    @EventListener(ContextClosedEvent.class)
    @Profile(Profiles.PRODUCTION)
    public void unsubscribeRegBotOnAppShutdown() {
    }
}
