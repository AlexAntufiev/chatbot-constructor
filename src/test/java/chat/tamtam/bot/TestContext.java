package chat.tamtam.bot;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import chat.tamtam.bot.domain.bot.BotSchemeEntity;
import chat.tamtam.bot.domain.bot.TamBotEntity;
import chat.tamtam.botapi.model.UserWithPhoto;

@AutoConfigureTestDatabase
public class TestContext {

    protected static final int BOT_SCHEME_ID = 1;
    protected static final Long USER_ID = 11L;
    protected static final int FAILED_BOT_ID = 2;
    protected static final String BOT_NAME = "BOT NAME";
    protected static final String AUTH_TOKEN = "AUTH";
    protected static final String LOGIN_ADMIN = "ADMIN";
    protected static final String PASSWORD_ADMIN = "ADMIN";

    protected static final BotSchemeEntity BOT_SCHEME_ENTITY;
    protected static final BotSchemeEntity BOT_SCHEME_ENTITY_WITH_TAM_BOT;

    protected static final Long TAM_BOT_ID = 2L;
    protected static final Long TAM_BOT_USER_ID = 3L;
    protected static final String TAM_BOT_NAME = "TAM_BOT_NAME";
    protected static final String TAM_BOT_USERNAME = "TAM_BOT_USERNAME";
    protected static final String TAM_BOT_TOKEN = "TAM_BOT_TOKEN";

    protected static final TamBotEntity.Id TAM_BOT_ID_ENTITY;
    protected static final TamBotEntity TAM_BOT_ENTITY;
    protected static final UserWithPhoto USER_WITH_PHOTO;

    static {
        BOT_SCHEME_ENTITY = new BotSchemeEntity(USER_ID, BOT_NAME);
        BOT_SCHEME_ENTITY_WITH_TAM_BOT = new BotSchemeEntity(USER_ID, BOT_NAME);
        BOT_SCHEME_ENTITY_WITH_TAM_BOT.setBotId(TAM_BOT_ID);
        USER_WITH_PHOTO = new UserWithPhoto(
                null,
                null,
                TAM_BOT_ID,
                TAM_BOT_NAME,
                TAM_BOT_USERNAME
        );
        TAM_BOT_ID_ENTITY = new TamBotEntity.Id(TAM_BOT_ID, USER_ID);
        TAM_BOT_ENTITY = new TamBotEntity(
                USER_ID,
                TAM_BOT_TOKEN,
                USER_WITH_PHOTO
        );
    }
}
