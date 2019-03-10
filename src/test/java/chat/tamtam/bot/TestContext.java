package chat.tamtam.bot;

import chat.tamtam.bot.domain.BotSchemaEntity;

public class TestContext {

    public static final int BOT_ID = 1;
    public static final Integer USER_ID = 11;
    public static final int FAILED_BOT_ID = 2;
    public static final String BOT_NAME = "BOT NAME";
    public static final String AUTH_TOKEN = "AUTH";
    public static final String LOGIN_ADMIN = "ADMIN";
    public static final String PASSWORD_ADMIN = "ADMIN";

    public static final BotSchemaEntity BOT_SCHEMA_ENTITY;

    static {
        BOT_SCHEMA_ENTITY = new BotSchemaEntity(USER_ID, BOT_NAME);
    }
}
