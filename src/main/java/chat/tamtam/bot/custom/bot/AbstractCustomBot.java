package chat.tamtam.bot.custom.bot;

import chat.tamtam.bot.configuration.AppProfiles;
import chat.tamtam.bot.controller.Endpoint;
import chat.tamtam.botapi.TamTamBotAPI;
import chat.tamtam.botapi.exceptions.APIException;
import chat.tamtam.botapi.exceptions.ClientException;
import chat.tamtam.botapi.model.AttachmentRequest;
import chat.tamtam.botapi.model.NewMessageBody;
import chat.tamtam.botapi.model.SimpleQueryResult;
import chat.tamtam.botapi.model.SubscriptionRequestBody;
import chat.tamtam.botapi.model.Update;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;

@Log4j2
public abstract class AbstractCustomBot {
    @Getter
    protected final String id;
    protected final String host;
    protected final TamTamBotAPI api;

    private final Environment environment;
    private final String token;
    private String url;

    protected AbstractCustomBot(
            final String id,
            final String token,
            final String host,
            final Environment environment
    ) {
        this.id = id;
        this.token = token;
        this.host = "http://" + host;
        this.environment = environment;
        api = token == null ? null : TamTamBotAPI.create(token);
    }

    public void process(Update update) {
        throw new UnsupportedOperationException("from " + getType());
    }

    public abstract BotType getType();

    protected static NewMessageBody messageOf(String message) {
        return new NewMessageBody(message, null);
    }

    protected static NewMessageBody messageOf(String message, List<AttachmentRequest> attachments) {
        return new NewMessageBody(message, attachments);
    }

    @PostConstruct
    protected void subscribe() {
        log.info(String.format("%s(id:%s, token:%s) initialized", getClass().getCanonicalName(), id, token));
        if (environment.acceptsProfiles(AppProfiles.noDevelopmentProfiles())) {
            try {
                url = host + Endpoint.TAM_CUSTOM_BOT_WEBHOOK + "/" + id;
                SimpleQueryResult result = api.subscribe(new SubscriptionRequestBody(url)).execute();
                if (result.isSuccess()) {
                    log.info(
                            String.format(
                                    "%s(id:%s, token:%s) subscribed on %s",
                                    getClass().getCanonicalName(), id, token, url
                            )
                    );
                } else {
                    log.warn(
                            String.format(
                                    "Can't subscribe %s(id:%s, token:%s) on %s",
                                    getClass().getCanonicalName(), id, token, url
                            )
                    );
                }
            } catch (ClientException | APIException e) {
                log.error(
                        String.format(
                                "Can't subscribe %s with id = [%s] via url = [%s]",
                                getClass().getCanonicalName(), id, url
                        ),
                        e
                );
            }
        }
    }

    @EventListener
    public void onRefreshScopeRefreshed(final RefreshScopeRefreshedEvent event) { }

    @PreDestroy
    public void unsubscribe() {
        if (environment.acceptsProfiles(AppProfiles.noDevelopmentProfiles())) {
            try {
                SimpleQueryResult result = api.unsubscribe(url).execute();
                log.info(
                        String.format(
                                "%s(id:%s, token:%s) unsubscribed from %s",
                                getClass().getCanonicalName(), id, token, url
                        )
                );
                if (!result.isSuccess()) {
                    log.warn(
                            String.format(
                                    "Can't unsubscribe %s(id:%s, token:%s) on %s",
                                    getClass().getCanonicalName(), id, token, url
                            )
                    );
                }
            } catch (ClientException | APIException e) {
                log.error(
                        String.format(
                                "Can't unsubscribe %s with id = [%s] via url = [%s]",
                                getClass().getCanonicalName(), id, url
                        ),
                        e
                );
            }
        }
    }
}
