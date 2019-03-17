package chat.tamtam.bot.integration.bot;

import java.util.Collections;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import com.fasterxml.jackson.databind.ObjectMapper;

import chat.tamtam.bot.configuration.Profiles;
import chat.tamtam.bot.controller.Endpoints;
import chat.tamtam.bot.repository.BotSchemaRepository;
import chat.tamtam.bot.repository.SessionRepository;

import static chat.tamtam.bot.TestContext.AUTH_TOKEN;
import static chat.tamtam.bot.TestContext.BOT_SCHEME_ENTITY;
import static chat.tamtam.bot.TestContext.USER_ID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Disabled
@ActiveProfiles(Profiles.TEST)
@SpringBootTest
@AutoConfigureMockMvc(secure = false)
public class BotScheme {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BotSchemaRepository botSchemaRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // @todo #CC-31 unignore mockmvc test
    @Test
    public void getBotScheme() throws Exception {
        String toJson = objectMapper.writeValueAsString(BOT_SCHEME_ENTITY);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.put(HttpHeaders.AUTHORIZATION, Collections.singletonList(AUTH_TOKEN));
        mockMvc.perform(get(String.format(Endpoints.API_BOT + "/%s", USER_ID)).accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.ALL)
                .headers(httpHeaders))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(content().json(toJson));
    }
}
