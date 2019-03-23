package chat.tamtam.bot.domain.broadcast.message.action;

import java.time.ZonedDateTime;

import chat.tamtam.bot.domain.broadcast.message.BroadcastMessageEntity;
import chat.tamtam.bot.domain.broadcast.message.BroadcastMessageState;
import chat.tamtam.bot.domain.broadcast.message.BroadcastMessageUpdate;
import chat.tamtam.bot.service.Error;

import static chat.tamtam.bot.domain.broadcast.message.utils.TimeUtils.getTimestamp;
import static chat.tamtam.bot.domain.broadcast.message.utils.TimeUtils.parseZonedDateTime;

public final class SentStateAction {
    private SentStateAction() { }

    public static void doAction(
            final BroadcastMessageEntity broadcastMessage,
            final BroadcastMessageUpdate broadcastMessageUpdate
    ) {
        if (broadcastMessageUpdate.getErasingTime() == null) {
            broadcastMessage.setErasingTime(null);
            broadcastMessage.setState(BroadcastMessageState.DISCARDED_ERASE_BY_USER);
            return;
        }
        broadcastMessage.setErasingTime(
                getTimestamp(
                        parseZonedDateTime(
                                broadcastMessageUpdate.getErasingTime(),
                                String.format(
                                        "Erasing utils is malformed, broadcast message id=%d",
                                        broadcastMessage.getId()
                                ),
                                Error.BROADCAST_MESSAGE_ERASING_TIME_IS_MALFORMED
                        ),
                        ZonedDateTime.now(),
                        "Erasing utils=%d is in the past and current utils=%d",
                        Error.BROADCAST_MESSAGE_ERASING_TIME_IS_IN_THE_PAST
                )
        );
    }
}
