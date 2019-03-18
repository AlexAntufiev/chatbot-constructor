package chat.tamtam.bot;

import chat.tamtam.bot.domain.BotSchemeEntity;
import chat.tamtam.bot.domain.TamBotEntity;
import chat.tamtam.botapi.model.UserWithPhoto;

public class TestContext {

    public static final int BOT_SCHEME_ID = 1;
    public static final Long USER_ID = 11L;
    public static final int FAILED_BOT_ID = 2;
    public static final String BOT_NAME = "BOT NAME";
    public static final String AUTH_TOKEN = "AUTH";
    public static final String LOGIN_ADMIN = "ADMIN";
    public static final String PASSWORD_ADMIN = "ADMIN";

    public static final BotSchemeEntity BOT_SCHEME_ENTITY;
    public static final BotSchemeEntity BOT_SCHEME_ENTITY_WITH_TAM_BOT;

    public static final Long TAM_BOT_ID = 2L;
    public static final Long TAM_BOT_USER_ID = 3L;
    public static final String TAM_BOT_NAME = "TAM_BOT_NAME";
    public static final String TAM_BOT_USERNAME = "TAM_BOT_USERNAME";
    public static final String TAM_BOT_TOKEN = "TAM_BOT_TOKEN";

    public static final TamBotEntity.Id TAM_BOT_ID_ENTITY;
    public static final TamBotEntity TAM_BOT_ENTITY;
    public static final UserWithPhoto USER_WITH_PHOTO;

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
