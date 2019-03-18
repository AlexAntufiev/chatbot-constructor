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
    String STATUS = "/status";

    String TAM_CONNECT = "/tam/connect";
    String TAM_DISCONNECT = "/tam/disconnect";

    String TAM_CHATCHANNEL = "/tam/chatchannel";
    String CHATCHANNEL_ID = "/{chatchannel_id}";
    String ADMIN = "/admin";
    String TAM_MARKER = "/{marker}";
    String STORE = "/store";

    String STATIC_INDEX = "/index.html";
    String STATIC_RESOURCES = "/assets/**";

    String HEALTH = "/actuator/health";

    String TAM_CUSTOM_BOT_WEBHOOK = "/tam/custom/bot";
    String TAM_BOT_WEBHOOK = "/tam/bot";
}
