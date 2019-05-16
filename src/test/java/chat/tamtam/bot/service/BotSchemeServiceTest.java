package chat.tamtam.bot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import chat.tamtam.bot.TestContext;
import chat.tamtam.bot.domain.bot.BotScheme;
import chat.tamtam.bot.domain.exception.NotFoundEntityException;
import chat.tamtam.bot.repository.BotSchemeRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class BotSchemeServiceTest extends TestContext {

    @Autowired
    private BotSchemeService botSchemeService;
    @MockBean
    private UserService userService;
    @MockBean
    private BotSchemeRepository botSchemeRepository;

    @BeforeEach
    void setUp() {
        when(userService.getUserIdByToken(eq(AUTH_TOKEN))).thenReturn(USER_ID);
    }

    @Test
    void getBot() {
        when(botSchemeRepository.findByUserIdAndId(eq(USER_ID), eq(BOT_SCHEME_ID))).thenReturn(BOT_SCHEME_ENTITY);
        BotScheme bot = botSchemeService.getBotScheme(AUTH_TOKEN, BOT_SCHEME_ID);
        assertNotNull(bot, "BotScheme entity must be return");
        assertEquals(USER_ID, bot.getUserId(), "User id must be set");
        assertEquals(BOT_NAME, bot.getName(), "BotScheme name must be set");
    }

    @Test
    void getNullBot() {
        when(botSchemeRepository.findByUserIdAndId(eq(USER_ID), eq(BOT_SCHEME_ID))).thenReturn(null);
        assertThrows(NotFoundEntityException.class, () -> botSchemeService.getBotScheme(AUTH_TOKEN, BOT_SCHEME_ID));
    }
}
