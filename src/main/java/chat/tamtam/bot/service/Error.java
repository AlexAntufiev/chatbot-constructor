package chat.tamtam.bot.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum Error {
    SERVICE_NO_ENTITY("errors.service.no.entity"),
    TAM_SERVICE_ERROR("errors.tam.service"),
    TAM_BOT_TOKEN_INCORRECT("errors.tam.bot.token.incorrect"),
    TAM_BOT_TOKEN_EMPTY("errors.tam.bot.token.empty"),
    TAM_BOT_SUBSCRIBED_ALREADY("errors.tam.bot.subscribed.already"),
    TAM_BOT_UNSUBSCRIBED_ALREADY("errors.tam.bot.unsubscribed.already"),
    TAM_BOT_NOT_SUBSCRIBED("errors.tam.bot.not.subscribed"),
    TAM_BOT_CONNECTED_TO_OTHER_BOT_SCHEME("errors.tam.bot.connected.to.other.bot.scheme"),

    CHATCHANNEL_SELECTED_EMPTY("errors.chatChannel.selected.empty"),
    CHATCHANNEL_PERMISSIONS_ERROR("errors.chatChannel.permission"),
    CHATCHANNEL_DOES_NOT_EXIST("errors.chatChannel.does.not.exist"),
    CHAT_NOT_CHANNEL("errors.not.chatChannel"),

    BROADCAST_MESSAGE_DOES_NOT_EXIST("errors.broadcast.messageOf.does.not.exist"),
    BROADCAST_MESSAGE_FIRING_TIME_IS_MALFORMED("errors.broadcast.messageOf.firing.time.is.malformed"),
    BROADCAST_MESSAGE_ERASING_TIME_IS_MALFORMED("errors.broadcast.messageOf.erasing.time.is.malformed"),
    BROADCAST_MESSAGE_FIRING_TIME_IS_IN_PAST("errors.broadcast.messageOf.firing.time.is.in.past"),
    BROADCAST_MESSAGE_FIRING_TIME_IS_NULL("errors.broadcast.messageOf.firing.time.is.null"),
    BROADCAST_MESSAGE_TITLE_IS_EMPTY("errors.broadcast.messageOf.title.is.empty"),
    BROADCAST_MESSAGE_TEXT_IS_EMPTY("errors.broadcast.messageOf.text.is.empty"),
    BROADCAST_MESSAGE_ERASING_TIME_IS_BEFORE_THEN_FIRING_TIME(
            "errors.broadcast.messageOf.erasing.time.is.before.then.firing.time"
    ),
    BROADCAST_MESSAGE_ERASING_TIME_IS_IN_THE_PAST(
            "errors.broadcast.messageOf.erasing.time.is.in.the.past"
    ),
    BROADCAST_MESSAGE_ILLEGAL_STATE("errors.broadcast.messageOf.illegal.state"),
    BROADCAST_MESSAGE_SEND_ERROR("errors.broadcast.messageOf.send.error"),
    BROADCAST_MESSAGE_ERASE_ERROR("errors.broadcast.messageOf.erase.error"),
    BROADCAST_MESSAGE_SEND_ALREADY_DISCARDED("errors.broadcast.messageOf.send.already.discarded"),
    BROADCAST_MESSAGE_ERASE_ALREADY_DISCARDED("errors.broadcast.messageOf.erase.already.discarded"),
    BROADCAST_MESSAGE_HAS_TOO_MUCH_ATTACHMENTS("errors.broadcast.messageOf.has.too.much.attachments"),

    ATTACHMENT_TYPE_EMPTY("errors.attachment.type.is.empty"),
    ATTACHMENT_TYPE_ILLEGAL("errors.attachment.type.is.illegal"),
    ATTACHMENT_UPLOAD_SERVICE_ERROR("errors.attachment.upload.service.error"),
    ATTACHMENT_DOES_NOT_EXIST("errors.attachment.does.not.exist"),
    ATTACHMENT_IDENTIFIER_IS_NOT_VALID("errors.attachment.token.is.not.valid"),

    BOT_SCHEME_INVALID_VALIDATOR("errors.bot.scheme.invalid.validator"),
    ;

    @Getter
    private final String errorKey;
}
