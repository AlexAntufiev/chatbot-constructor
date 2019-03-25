package chat.tamtam.bot.domain.broadcast.message.action;

import java.time.ZonedDateTime;

import org.springframework.stereotype.Component;

import chat.tamtam.bot.domain.broadcast.message.BroadcastMessageEntity;
import chat.tamtam.bot.domain.broadcast.message.BroadcastMessageState;
import chat.tamtam.bot.domain.broadcast.message.BroadcastMessageUpdate;
import chat.tamtam.bot.service.Error;
import lombok.NoArgsConstructor;

@Component
@NoArgsConstructor
public final class ScheduledStateAction extends BroadcastMessageStateAction {
    @Override
    public void doAction(
            final BroadcastMessageEntity broadcastMessage,
            final BroadcastMessageUpdate broadcastMessageUpdate
    ) {
        if (broadcastMessageUpdate.getFiringTime() == null) {
            broadcastMessage.setFiringTime(null);
            broadcastMessage.setErasingTime(null);
            broadcastMessage.setState(BroadcastMessageState.CREATED);
            return;
        }

        ZonedDateTime firingTime = getDateTimeAtLocalZone(
                broadcastMessageUpdate.getFiringTime(),
                Error.BROADCAST_MESSAGE_FIRING_TIME_IS_MALFORMED
        );

        ZonedDateTime currentTime = ZonedDateTime.now();

        broadcastMessage.setFiringTime(
                futureTimestamp(
                        firingTime,
                        currentTime,
                        broadcastMessage.getId(),
                        Error.BROADCAST_MESSAGE_FIRING_TIME_IS_IN_PAST
                )
        );

        if (broadcastMessageUpdate.getErasingTime() == null) {
            broadcastMessage.setErasingTime(null);
            return;
        }

        ZonedDateTime erasingTime = getDateTimeAtLocalZone(
                broadcastMessageUpdate.getErasingTime(),
                Error.BROADCAST_MESSAGE_ERASING_TIME_IS_MALFORMED
        );

        broadcastMessage.setErasingTime(
                futureTimestamp(
                        erasingTime,
                        firingTime,
                        broadcastMessage.getId(),
                        Error.BROADCAST_MESSAGE_ERASING_TIME_IS_BEFORE_THEN_FIRING_TIME
                )
        );
    }
}
