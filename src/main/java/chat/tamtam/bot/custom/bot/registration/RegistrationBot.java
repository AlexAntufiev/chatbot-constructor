package chat.tamtam.bot.custom.bot.registration;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;

import chat.tamtam.bot.configuration.AppProfiles;
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

@Log4j2
@Component
@RefreshScope
@RequiredArgsConstructor
public class RegistrationBot extends AbstractCustomBot {
    private static final String HELP_MESSAGE =
            "Available commands:\n\n"
                    + "/reg - create user\n"
                    + "/del - delete user\n"
                    + "/upd {new password} - change password on specified one\n"
                    + "/login - receive auto-login button";

    private final @NonNull UserRepository userRepository;
    private final @NonNull SessionRepository sessionRepository;
    private final @NonNull BCryptPasswordEncoder bCryptPasswordEncoder;
    private final @NonNull Environment environment;

    @Getter
    @Value("${tamtam.bot.registration.id}")
    private String id;
    // @todo #CC-91 dont create reg bot with nullable id and token
    @Value("${tamtam.bot.registration.token}")
    private String token;

    @Value("${tamtam.host}")
    private String host;

    @Value("${tamtam.bot.registration.enabledIds}")
    private String ids;
    private final EnabledIdsConverter enabledIdsConverter;
    private EnabledIds enabledIds;

    private String url;
    private TamTamBotAPI botAPI;

    private RegistrationBotVisitor visitor;

    @PostConstruct
    public void subscribe() {
        enabledIds = enabledIdsConverter.convert(ids);
        botAPI = TamTamBotAPI.create(token);
        visitor = new RegistrationBotVisitor();
        log.info(String.format("Registration bot(id:%s, token:%s) initialized", id, token));
        if (environment.acceptsProfiles(AppProfiles.noDevelopmentProfiles())) {
            try {
                url = host + Endpoint.TAM_CUSTOM_BOT_WEBHOOK + "/" + id;
                SimpleQueryResult result = botAPI.subscribe(new SubscriptionRequestBody(url)).execute();
                if (result.isSuccess()) {
                    log.info(String.format("Registration bot(id:%s, token:%s) subscribed on %s", id, token, url));
                } else {
                    log.warn(String.format("Can't subscribe registration bot(id:%s, token:%s) on %s", id, token, url));
                }
            } catch (ClientException | APIException e) {
                log.error(String.format("Can't subscribe bot with id = [%s] via url = [%s]", id, url), e);
            }
        }
    }

    @PreDestroy
    public void unsubscribe() {
        if (environment.acceptsProfiles(AppProfiles.noDevelopmentProfiles())) {
            try {
                SimpleQueryResult result = botAPI.unsubscribe(url).execute();
                log.info(String.format("Registration bot(id:%s, token:%s) unsubscribed from %s", id, token, url));
                if (!result.isSuccess()) {
                    log.warn(String.format(
                            "Can't unsubscribe registration bot(id:%s, token:%s) on %s",
                            id,
                            token,
                            url
                    ));
                }
            } catch (ClientException | APIException e) {
                log.error(String.format("Can't unsubscribe bot with id = [%s] via url = [%s]", id, url), e);
            }
        }
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
                botAPI.sendMessage(messageBody)
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
                botAPI.sendMessage(messageOf(HELP_MESSAGE))
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
            case "/reg":
                return registrate(message.getSender().getUserId().toString());
            case "/del":
                return delete(message.getSender().getUserId().toString());
            case "/upd":
                if (cmd.length < 2) {
                    return messageOf("Please provide new password");
                } else {
                    return updatePassword(message.getSender().getUserId().toString(), cmd[1]);
                }
            case "/login":
                return login(message.getSender().getUserId().toString());
            default:
                return messageOf(HELP_MESSAGE);
        }
    }

    private NewMessageBody login(final String userId) {
        UserEntity user = userRepository.findByLogin(userId);
        if (user != null) {
            return messageOf("Press to sign in", List.of(getAutoLoginButton(userId)));
        } else {
            return messageOf("Login: " + userId + " not found\nTry '/reg' to create user");
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
            return messageOf(response, List.of(getAutoLoginButton(userId)));
        }
        String response = "Login: " + userId + "\nTry '/upd' to change password";
        return messageOf(response, List.of(getAutoLoginButton(userId)));
    }

    private NewMessageBody delete(final String userId) {
        UserEntity user = userRepository.findByLogin(userId);
        if (user != null) {
            userRepository.removeByLogin(userId);
            sessionRepository.removeAllByLogin(userId);
            return messageOf("User with login: " + userId + " deleted");
        }
        return messageOf("User with login: " + userId + " not found\nTry '/reg' to create user");
    }

    private NewMessageBody updatePassword(final String userId, final String newPassword) {
        UserEntity user = userRepository.findByLogin(userId);
        if (user != null) {
            if (newPassword.isEmpty()) {
                return messageOf("Try another password");
            }
            user.setPasswordHash(bCryptPasswordEncoder.encode(newPassword));
            userRepository.save(user);
            String response = "Login: " + userId + "\nPassword: " + newPassword;
            sessionRepository.removeAllByLogin(userId);
            return messageOf(response, List.of(getAutoLoginButton(userId)));
        }
        return messageOf("Login: " + userId + " not found\nTry '/reg' to create user");
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
