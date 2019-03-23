package chat.tamtam.bot.domain.broadcast.message.action;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import chat.tamtam.bot.domain.broadcast.message.BroadcastMessageEntity;
import chat.tamtam.bot.domain.broadcast.message.BroadcastMessageState;
import chat.tamtam.bot.domain.broadcast.message.BroadcastMessageUpdate;
import chat.tamtam.bot.service.Error;

import static chat.tamtam.bot.domain.broadcast.message.utils.TimeUtils.getTimestamp;
import static chat.tamtam.bot.domain.broadcast.message.utils.TimeUtils.parseZonedDateTime;

public final class CreatedStateAction {
    private CreatedStateAction() { }

    public static void doAction(
            final BroadcastMessageEntity broadcastMessage,
            final BroadcastMessageUpdate broadcastMessageUpdate
    ) {
        // set firing utils
        if (broadcastMessageUpdate.getFiringTime() != null) {
            broadcastMessage.setFiringTime(getTimestamp(
                    parseZonedDateTime(
                            broadcastMessageUpdate.getFiringTime(),
                            String.format(
                                    "Malformed firing utils, broadcast message id=%d",
                                    broadcastMessage.getId()
                            ),
                            Error.BROADCAST_MESSAGE_FIRING_TIME_IS_MALFORMED
                    ),
                    ZonedDateTime.now(),
                    "BroadCastMessage firing utils=%d is in the past and current utils=%d",
                    Error.BROADCAST_MESSAGE_FIRING_TIME_IS_IN_PAST
            ));
            // set erasing utils
            if (broadcastMessageUpdate.getErasingTime() != null) {
                broadcastMessage.setErasingTime(getTimestamp(
                        parseZonedDateTime(
                                broadcastMessageUpdate.getErasingTime(),
                                String.format(
                                        "Malformed erasing utils, broadcast message id=%d",
                                        broadcastMessage.getId()
                                ),
                                Error.BROADCAST_MESSAGE_ERASING_TIME_IS_MALFORMED
                        ),
                        ZonedDateTime.ofInstant(
                                broadcastMessage.getFiringTime().toInstant(),
                                ZoneId.systemDefault()
                        ),
                        "BroadCastMessage erasing utils=%d is before then firing utils=%d",
                        Error.BROADCAST_MESSAGE_ERASING_TIME_IS_BEFORE_THEN_FIRING_TIME
                ));
            }
            broadcastMessage.setState(BroadcastMessageState.SCHEDULED);
        }
    }
}
