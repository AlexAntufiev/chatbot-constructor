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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;

import chat.tamtam.bot.configuration.Profiles;
import chat.tamtam.bot.controller.Endpoint;
import chat.tamtam.bot.converter.EnabledIds;
import chat.tamtam.bot.converter.EnabledIdsConverter;
import chat.tamtam.bot.custom.bot.AbstractCustomBot;
import chat.tamtam.bot.custom.bot.BotType;
import chat.tamtam.bot.domain.session.SessionEntity;
import chat.tamtam.bot.domain.user.UserEntity;
import chat.tamtam.bot.repository.SessionRepository;
import chat.tamtam.bot.repository.UserRepository;
import chat.tamtam.bot.security.SecurityConstants;
import chat.tamtam.botapi.TamTamBotAPI;
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
import chat.tamtam.botapi.model.SimpleQueryResult;
import chat.tamtam.botapi.model.SubscriptionRequestBody;
import chat.tamtam.botapi.model.Update;
import chat.tamtam.botapi.model.UserAddedToChatUpdate;
import chat.tamtam.botapi.model.UserRemovedFromChatUpdate;
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
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @Value("${tamtam.registration.bot.id}")
    private String registrationBotId;
    // @todo #CC-91 dont create reg bot with nullable registrationBotId and token
    @Getter
    @Value("${tamtam.registration.bot.token}")
    private String token;

    @Value("${tamtam.host}")
    private String host;

    @Value("${tamtam.registration.bot.enabledIds:}")
    private String ids;
    private final EnabledIdsConverter enabledIdsConverter;
    private EnabledIds enabledIds;

    private String url;
    private boolean subscribed = false;
    private TamTamBotAPI botAPI;

    private RegistrationBotVisitor visitor;

    @PostConstruct
    public void init() {
        enabledIds = enabledIdsConverter.convert(ids);
        botAPI = TamTamBotAPI.create(token);
        visitor = new RegistrationBotVisitor();
        log.info(
                String.format(
                        "Registration bot(id:%s, token:%s) initialized",
                        registrationBotId,
                        token
                )
        );
    }

    @Override
    public void process(final Update update) {
        log.debug(String.format(
                "Registration bot event(type=%s)",
                update.getType()
        ));
        update.visit(visitor);
    }

    private void response(final MessageCreatedUpdate update) {
        try {
            if (filter(update.getMessage().getSender().getUserId())) {
                NewMessageBody messageBody = resolve(update.getMessage());
                botAPI.sendMessage(messageBody)
                        .userId(update.getMessage().getSender().getUserId())
                        .execute();
            }
        } catch (APIException | ClientException e) {
            log.error(
                    String.format(
                            "Bot(id=%s) can't response to event(type:%s, id:%s, sender:%d)",
                            id,
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
                botAPI.sendMessage(new NewMessageBody(HELP_MESSAGE, Collections.emptyList()))
                        .userId(update.getUserId())
                        .execute();
            }
        } catch (APIException | ClientException e) {
            log.error(
                    String.format(
                            "Bot(id=%s) can't response to event(type:%s, sender:%d)",
                            id,
                            update.getType(),
                            update.getUserId()
                    ),
                    e
            );
        }
    }

    private boolean filter(final Message message) {
        return enabledIds.isEnabled(message.getSender().getUserId());
    }

    private NewMessageBody resolve(final Message message) {
        String[] cmd = Optional
                .ofNullable(message.getBody().getText())
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
            // @todo #CC-36 Improve password generation
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
    public String getId() {
        return registrationBotId;
    }

    @Override
    public BotType getType() {
        return BotType.Registration;
    }

    @Profile({Profiles.PRODUCTION, Profiles.TEST})
    @Bean
    public void subscribeRegBotOnAppReadyProduction() {
        try {
            url = host + Endpoint.TAM_CUSTOM_BOT_WEBHOOK + "/" + registrationBotId;
            SimpleQueryResult result = botAPI.subscribe(new SubscriptionRequestBody(url)).execute();
            if (result.isSuccess()) {
                log.info(String.format("Registration bot(registrationBotId:%d, token:%s) subscribed on %s",
                        registrationBotId, token, url
                ));
            } else {
                log.warn(String.format("Can't subscribe registration bot(registrationBotId:%d, token:%s) on %s",
                        registrationBotId, token, url
                ));
            }
            subscribed = true;
        } catch (ClientException | APIException e) {
            log.error(String.format("Can't subscribe bot with registrationBotId = [%s] via url = [%s]",
                    registrationBotId, url
            ), e);
        }
    }

    @Profile({Profiles.PRODUCTION, Profiles.TEST})
    @PreDestroy
    public void unsubscribeRegBotOnAppShutdown() {
        if (!subscribed) {
            return;
        }
        try {
            SimpleQueryResult result = botAPI.unsubscribe(url).execute();
            log.info(String.format("Registration bot(registrationBotId:%s, token:%s) unsubscribed from %s",
                    registrationBotId, token, url
            ));
            if (!result.isSuccess()) {
                log.warn(String.format("Can't unsubscribe registration bot(registrationBotId:%s, token:%s) on %s",
                        registrationBotId, token, url
                ));
            }
        } catch (ClientException | APIException e) {
            log.error(String.format("Can't unsubscribe bot with registrationBotId = [%s] via url = [%s]",
                    registrationBotId, url
            ), e);
        }
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
