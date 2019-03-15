package chat.tamtam.bot;

import chat.tamtam.bot.domain.BotSchemeEntity;

public class TestContext {

    public static final int BOT_ID = 1;
    public static final Long USER_ID = 11L;
    public static final int FAILED_BOT_ID = 2;
    public static final String BOT_NAME = "BOT NAME";
    public static final String AUTH_TOKEN = "AUTH";
    public static final String LOGIN_ADMIN = "ADMIN";
    public static final String PASSWORD_ADMIN = "ADMIN";

    public static final BotSchemeEntity BOT_SCHEMA_ENTITY;

    static {
        BOT_SCHEMA_ENTITY = new BotSchemeEntity(USER_ID, BOT_NAME);
    }
}
