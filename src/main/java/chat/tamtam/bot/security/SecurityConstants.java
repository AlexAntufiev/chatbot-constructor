package chat.tamtam.bot.security;

public interface SecurityConstants {
    long EXPIRATION_TIME = 864_000_000; // 10 days
    String SECRET = "That_Was_Hard_But_Per_Aspera_Ad_Astra";
    String TOKEN_PREFIX = "Astra";
    String COOKIE_AUTH = "authorization";
    String COOKIE_USER_ID = "userId";
    String ACCESS_TOKEN_PARAM = "access_token";
}
