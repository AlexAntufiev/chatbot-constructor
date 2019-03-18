package chat.tamtam.bot.service;

public interface Errors {
    String SERVICE_ERROR = "errors.service.no.entity";

    String TAM_SERVICE_ERROR = "errors.tam.service";
    String TAM_BOT_TOKEN_INCORRECT = "errors.tam.bot.token.incorrect";
    String TAM_BOT_TOKEN_EMPTY = "errors.tam.bot.token.empty";
    String TAM_BOT_SUBSCRIBED_ALREADY = "errors.tam.bot.subscribed.already";
    String TAM_BOT_UNSUBSCRIBED_ALREADY = "errors.tam.bot.unsubscribed.already";

    String TAM_BOT_NOT_SUBSCRIBED = "errors.tam.bot.not.subscribed";

    String CHATCHANNEL_SELECTED_EMPTY = "errors.chatChannel.selected.empty";
    String CHATCHANNEL_PERMISSIONS_ERROR = "errors.chatChannel.permission";
    String CHATCHANNEL_NOT_EXIST = "errors.chatChannel.not.exist";
    String CHAT_NOT_CHANNEL = "errors.not.chatChannel";

}
