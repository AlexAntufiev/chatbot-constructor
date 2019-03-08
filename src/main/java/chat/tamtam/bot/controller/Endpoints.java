package chat.tamtam.bot.controller;

public interface Endpoints {
    String API_LOGIN = "/api/login";
    String API_LOGOUT = "/api/logout";
    String API_REGISTRATION = "/api/registration";
    String API_BOT = "/api/bot";
    String ID = "/{id}";
    String LIST = "/list";
    String ADD = "/add";
    String DELETE = "/delete";
    String ID_SAVE = "/{id}/save";
    String ID_CONNECT = "/{id}/connect";
    String ID_DISCONNECT = "/{id}/disconnect";

    String STATIC_INDEX = "/index.html";
    String STATIC_RESOURCES = "/assets/**";

    String HEALTH = "/actuator/health";
}
