package chat.tamtam.bot.service;

import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import chat.tamtam.bot.RunnableTestContext;
import chat.tamtam.bot.domain.BotSchemaEntity;
import chat.tamtam.bot.repository.BotSchemaRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class BotServiceTest extends RunnableTestContext {

    @Autowired
    private BotService botService;
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
        when(botSchemaRepository.findByUserIdAndId(eq(USER_ID), eq(BOT_ID)))
                .thenReturn(BOT_SCHEMA_ENTITY);
        BotSchemaEntity bot = botService.getBot(AUTH_TOKEN, BOT_ID);
        assertNotNull(bot, "Bot entity must be return");
        assertEquals(USER_ID, bot.getUserId(), "User id must be set");
        assertEquals(BOT_NAME, bot.getName(), "Bot name must be set");
    }

    @Test
    void getNullBot() {
        when(botSchemaRepository.findByUserIdAndId(eq(USER_ID), eq(BOT_ID)))
                .thenReturn(null);
        assertThrows(NoSuchElementException.class, () -> botService.getBot(AUTH_TOKEN, BOT_ID));
    }
}
