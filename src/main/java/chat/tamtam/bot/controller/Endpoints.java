package chat.tamtam.bot.controller;

public interface Endpoints {
    String API_LOGIN = "/api/login";
    String API_REGISTRATION = "/api/registration";
    String API_BOT_INFO = "/api/bot/{id}";
    String API_BOT_LIST = "/api/bot/list";
    String API_BOT_ADD = "/api/bot/add";
    String API_BOT_DELETE = "/api/bot/delete";
    String API_BOT_SAVE = "/api/bot/{id}/save";
    String API_BOT_CONNECT = "/api/bot/{id}/connect";
    String API_BOT_DISCONNECT = "/api/bot/{id}/disconnect";

    String STATIC_INDEX = "/index.html";
    String STATIC_RESOURCES = "/assets/**";

    String HEALTH = "/actuator/health";
}
