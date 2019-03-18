package chat.tamtam.bot.controller;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import chat.tamtam.bot.RunnableTestContext;
import chat.tamtam.bot.domain.BotSchemeEntity;
import chat.tamtam.bot.service.BotSchemeService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;

class BotSchemeControllerTest extends RunnableTestContext {

    @Autowired
    private BotSchemeController botSchemeController;

    @MockBean
    private BotSchemeService botSchemeService;

    @Test
    @SuppressWarnings("CastToConcreteClass")
    public void getBotScheme() {
        Mockito.when(botSchemeService.getBotScheme(ArgumentMatchers.anyString(), eq(BOT_SCHEME_ID)))
                .thenReturn(BOT_SCHEME_ENTITY);

        ResponseEntity<?> responseEntity = botSchemeController.getBotScheme(BOT_SCHEME_ID, AUTH_TOKEN);

        assertNotNull(responseEntity, "Controller must be return object");
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode(), "Status code must be return");
        BotSchemeEntity body = (BotSchemeEntity) responseEntity.getBody();
        assertNotNull(body, "Body must be null");
        assertEquals(USER_ID, body.getUserId(), "User id must be set");
        assertEquals(BOT_NAME, body.getName(), "BotScheme name must be set");
    }
}
