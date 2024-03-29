package chat.tamtam.bot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import chat.tamtam.bot.TestContext;
import chat.tamtam.bot.domain.bot.TamBotEntity;
import chat.tamtam.bot.domain.exception.ChatBotConstructorException;
import chat.tamtam.bot.domain.exception.NotFoundEntityException;
import chat.tamtam.bot.repository.TamBotRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class TamBotServiceTest extends TestContext {
    @Autowired
    private TamBotService tamBotService;

    @MockBean
    private UserService userService;
    @MockBean
    private BotSchemeService botSchemeService;
    @MockBean
    private TamBotRepository tamBotRepository;

    @BeforeEach
    void setUp() {
        when(userService.getUserIdByToken(eq(AUTH_TOKEN))).thenReturn(USER_ID);
    }

    @Test
    public void getStatusTest() {
        when(botSchemeService.getBotScheme(eq(AUTH_TOKEN), eq(BOT_SCHEME_ID)))
                .thenReturn(BOT_SCHEME_ENTITY);

        assertThrows(
                ChatBotConstructorException.class,
                () -> tamBotService.status(AUTH_TOKEN, BOT_SCHEME_ID),
                "Should throws ChatBotConstructorException"
        );

        when(botSchemeService.getBotScheme(eq(AUTH_TOKEN), eq(BOT_SCHEME_ID)))
                .thenReturn(BOT_SCHEME_ENTITY_WITH_TAM_BOT);
        when(tamBotRepository.findById(eq(TAM_BOT_ID_ENTITY)))
                .thenReturn(null);

        assertThrows(
                NotFoundEntityException.class,
                () -> tamBotService.status(AUTH_TOKEN, BOT_SCHEME_ID),
                "Should throws NotFoundEntityException"
        );

        when(tamBotRepository.findById(eq(TAM_BOT_ID_ENTITY)))
                .thenReturn(TAM_BOT_ENTITY);

        TamBotEntity tamBot = tamBotService.status(AUTH_TOKEN, BOT_SCHEME_ID);

        assertNotNull(tamBot, "Should return tam bot entity");
        assertEquals(USER_ID, tamBot.getId().getUserId(), "Ids should be equal");
        assertEquals(TAM_BOT_ID, tamBot.getId().getBotId(), "Ids should be equal");
        assertEquals(TAM_BOT_TOKEN, tamBot.getToken(), "Tokens should be equal");
    }

}
