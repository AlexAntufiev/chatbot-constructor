package chat.tamtam.bot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import chat.tamtam.bot.RunnableTestContext;
import chat.tamtam.bot.domain.BotSchemeEntity;
import chat.tamtam.bot.domain.exception.NotFoundEntityException;
import chat.tamtam.bot.repository.BotSchemaRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class BotSchemeServiceTest extends RunnableTestContext {

    @Autowired
    private BotSchemeService botSchemeService;
    @MockBean
    private UserService userService;
    @MockBean
    private BotSchemaRepository botSchemaRepository;

    @BeforeEach
    void setUp() {
        when(userService.getUserIdByToken(eq(AUTH_TOKEN))).thenReturn(USER_ID);
    }

    @Test
    void getBot() {
        when(botSchemaRepository.findByUserIdAndId(eq(USER_ID), eq(BOT_SCHEME_ID))).thenReturn(BOT_SCHEME_ENTITY);
        BotSchemeEntity bot = botSchemeService.getBotScheme(AUTH_TOKEN, BOT_SCHEME_ID);
        assertNotNull(bot, "BotScheme entity must be return");
        assertEquals(USER_ID, bot.getUserId(), "User id must be set");
        assertEquals(BOT_NAME, bot.getName(), "BotScheme name must be set");
    }

    @Test
    void getNullBot() {
        when(botSchemaRepository.findByUserIdAndId(eq(USER_ID), eq(BOT_SCHEME_ID))).thenReturn(null);
        assertThrows(NotFoundEntityException.class, () -> botSchemeService.getBotScheme(AUTH_TOKEN, BOT_SCHEME_ID));
    }
}
