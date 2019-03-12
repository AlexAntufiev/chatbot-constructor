package chat.tamtam.bot.controller;

import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import chat.tamtam.bot.RunnableTestContext;
import chat.tamtam.bot.domain.BotSchemaEntity;
import chat.tamtam.bot.service.BotSchemeService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;

class BotSchemeControllerTest extends RunnableTestContext {

    @Autowired
    private BotSchemeController botSchemeController;

    @MockBean
    private BotSchemeService botSchemeService;

    @Test
    void getNullBotScheme() {
        Mockito.when(botSchemeService.getBot(ArgumentMatchers.anyString(), eq(FAILED_BOT_ID)))
                .thenThrow(NoSuchElementException.class);

        ResponseEntity<?> responseEntity = botSchemeController.getBotScheme(FAILED_BOT_ID, AUTH_TOKEN);

        assertNotNull(responseEntity, "Controller must be return object");
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode(), "Status code must be return");
        assertNull(responseEntity.getBody(), "Body must be null");
    }

    @Test
    @SuppressWarnings("CastToConcreteClass")
    public void getBotScheme() {
        Mockito.when(botSchemeService.getBot(ArgumentMatchers.anyString(), eq(BOT_ID)))
                .thenReturn(BOT_SCHEMA_ENTITY);

        ResponseEntity<?> responseEntity = botSchemeController.getBotScheme(BOT_ID, AUTH_TOKEN);

        assertNotNull(responseEntity, "Controller must be return object");
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode(), "Status code must be return");
        BotSchemaEntity body = (BotSchemaEntity) responseEntity.getBody();
        assertNotNull(body, "Body must be null");
        assertEquals(USER_ID, body.getUserId(), "User id must be set");
        assertEquals(BOT_NAME, body.getName(), "BotScheme name must be set");
    }
}
