package chat.tamtam.bot.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum Error {
    SERVICE_ERROR("errors.service.no.entity"),
    TAM_SERVICE_ERROR("errors.tam.service"),
    TAM_BOT_TOKEN_INCORRECT("errors.tam.bot.token.incorrect"),
    TAM_BOT_TOKEN_EMPTY("errors.tam.bot.token.empty"),
    TAM_BOT_SUBSCRIBED_ALREADY("errors.tam.bot.subscribed.already"),
    TAM_BOT_UNSUBSCRIBED_ALREADY("errors.tam.bot.unsubscribed.already"),
    TAM_BOT_NOT_SUBSCRIBED("errors.tam.bot.not.subscribed"),

    CHATCHANNEL_SELECTED_EMPTY("errors.chatChannel.selected.empty"),
    CHATCHANNEL_PERMISSIONS_ERROR("errors.chatChannel.permission"),
    CHATCHANNEL_DOES_NOT_EXIST("errors.chatChannel.does.not.exist"),
    CHAT_NOT_CHANNEL("errors.not.chatChannel"),

    BROADCAST_MESSAGE_DOES_NOT_EXIST("errors.broadcast.message.does.not.exist"),
    BROADCAST_MESSAGE_FIRING_TIME_IS_IN_PAST("errors.broadcast.message.firing.time.is.in.past"),
    BROADCAST_MESSAGE_FIRING_TIME_IS_NULL("errors.broadcast.message.firing.time.is.null"),
    BROADCAST_MESSAGE_TITLE_IS_EMPTY("errors.broadcast.message.title.is.empty"),
    BROADCAST_MESSAGE_ERASING_TIME_IS_BEFORE_THEN_FIRING_TIME(
            "errors.broadcast.message.erasing.time.is.before.then.firing.time"
    ),
    BROADCAST_MESSAGE_ILLEGAL_STATE("errors.broadcast.message.illegal.state"),
    BROADCAST_MESSAGE_SEND_ERROR("errors.broadcast.message.send.error"),
    BROADCAST_MESSAGE_ERASE_ERROR("errors.broadcast.message.erase.error"),
    ;

    @Getter
    private final String errorKey;
}
