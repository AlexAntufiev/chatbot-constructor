package chat.tamtam.bot.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum Error {
    SERVICE_ERROR("errors.service.error"),
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

    BROADCAST_MESSAGE_DOES_NOT_EXIST("errors.broadcast.message.does.not.exist"),
    BROADCAST_MESSAGE_FIRING_TIME_IS_MALFORMED("errors.broadcast.message.firing.time.is.malformed"),
    BROADCAST_MESSAGE_ERASING_TIME_IS_MALFORMED("errors.broadcast.message.erasing.time.is.malformed"),
    BROADCAST_MESSAGE_FIRING_TIME_IS_IN_PAST("errors.broadcast.message.firing.time.is.in.past"),
    BROADCAST_MESSAGE_FIRING_TIME_IS_NULL("errors.broadcast.message.firing.time.is.null"),
    BROADCAST_MESSAGE_TITLE_IS_EMPTY("errors.broadcast.message.title.is.empty"),
    BROADCAST_MESSAGE_TEXT_IS_EMPTY("errors.broadcast.message.text.is.empty"),
    BROADCAST_MESSAGE_ERASING_TIME_IS_BEFORE_THEN_FIRING_TIME(
            "errors.broadcast.message.erasing.time.is.before.then.firing.time"
    ),
    BROADCAST_MESSAGE_ERASING_TIME_IS_IN_THE_PAST(
            "errors.broadcast.message.erasing.time.is.in.the.past"
    ),
    BROADCAST_MESSAGE_ILLEGAL_STATE("errors.broadcast.message.illegal.state"),
    BROADCAST_MESSAGE_SEND_ERROR("errors.broadcast.message.send.error"),
    BROADCAST_MESSAGE_ERASE_ERROR("errors.broadcast.message.erase.error"),
    BROADCAST_MESSAGE_SEND_ALREADY_DISCARDED("errors.broadcast.message.send.already.discarded"),
    BROADCAST_MESSAGE_ERASE_ALREADY_DISCARDED("errors.broadcast.message.erase.already.discarded"),
    BROADCAST_MESSAGE_HAS_TOO_MUCH_ATTACHMENTS("errors.broadcast.message.has.too.much.attachments"),

    ATTACHMENT_TYPE_EMPTY("errors.attachment.type.is.empty"),
    ATTACHMENT_TYPE_ILLEGAL("errors.attachment.type.is.illegal"),
    ATTACHMENT_UPLOAD_SERVICE_ERROR("errors.attachment.upload.service.error"),
    ATTACHMENT_DOES_NOT_EXIST("errors.attachment.does.not.exist"),
    ATTACHMENT_IDENTIFIER_IS_NOT_VALID("errors.attachment.token.is.not.valid"),

    SCHEME_BUILDER_COMPONENT_ID_IS_NULL("errors.bot.scheme.builder.component.id.is.null"),
    SCHEME_BUILDER_COMPONENT_DUPLICATION("errors.bot.scheme.builder.component.duplication"),
    SCHEME_BUILDER_COMPONENT_DOES_NOT_EXIST("errors.bot.scheme.builder.component.is.absent"),
    SCHEME_BUILDER_COMPONENT_GRAPH_IS_CYCLIC("errors.bot.scheme.builder.component.graph.is.cyclic"),
    SCHEME_BUILDER_COMPONENT_TEXT_IS_EMPTY("errors.bot.scheme.builder.component.text.is.empty"),

    SCHEME_BUILDER_INVALID_VALIDATOR("errors.bot.scheme.invalid.componentValidator"),
    SCHEME_BUILDER_BUTTONS_EMPTY_FIELDS("errors.bot.scheme.builder.buttons.empty.fields"),
    SCHEME_BUILDER_BUTTONS_UPDATE_BY_ID("errors.bot.scheme.builder.buttons.id.not.exist"),
    SCHEME_BUILDER_BUTTONS_GROUP_IS_EMPTY("errors.bot.scheme.builder.buttons.group.is.empty"),
    SCHEME_BUILDER_BUTTONS_GROUP_INTENT_MALFORMED("errors.bot.scheme.builder.buttons.group.intent.malformed"),
    ;

    @Getter
    private final String errorKey;
}
