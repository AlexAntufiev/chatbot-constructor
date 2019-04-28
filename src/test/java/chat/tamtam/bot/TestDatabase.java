package chat.tamtam.bot;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;

import chat.tamtam.bot.domain.session.SessionEntity;
import chat.tamtam.bot.domain.user.UserAuthEntity;
import chat.tamtam.bot.repository.BotSchemeRepository;
import chat.tamtam.bot.repository.SessionRepository;
import chat.tamtam.bot.service.UserService;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TestDatabase extends TestContext {

    private static final int AMOUNT_TO_ADD = 3;
    private final BotSchemeRepository botSchemaRepository;
    private final SessionRepository sessionRepository;
    private final UserService userService;

    @Bean
    public InitializingBean initializeTestDatabase() {

        return () -> {
            botSchemaRepository.save(BOT_SCHEME_ENTITY);

            userService.addUser(new UserAuthEntity(LOGIN_ADMIN, PASSWORD_ADMIN));

            sessionRepository.save(new SessionEntity(AUTH_TOKEN,
                    USER_ID,
                    LOGIN_ADMIN,
                    Date.from(Instant.now().plus(AMOUNT_TO_ADD, ChronoUnit.DAYS))
            ));
        };
    }
}
