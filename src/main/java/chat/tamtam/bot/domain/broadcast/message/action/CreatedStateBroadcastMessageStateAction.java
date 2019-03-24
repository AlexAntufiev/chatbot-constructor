package chat.tamtam.bot.domain.broadcast.message.action;

import java.sql.Timestamp;
import java.time.ZonedDateTime;

import org.springframework.stereotype.Component;

import chat.tamtam.bot.domain.broadcast.message.BroadcastMessageEntity;
import chat.tamtam.bot.domain.broadcast.message.BroadcastMessageState;
import chat.tamtam.bot.domain.broadcast.message.BroadcastMessageUpdate;
import chat.tamtam.bot.domain.exception.UpdateBroadcastMessageException;
import chat.tamtam.bot.service.Error;
import lombok.NoArgsConstructor;

@Component
@NoArgsConstructor
public final class CreatedStateBroadcastMessageStateAction extends BroadcastMessageStateAction {
    @Override
    public void doAction(
            final BroadcastMessageEntity broadcastMessage,
            final BroadcastMessageUpdate broadcastMessageUpdate
    ) {
        ZonedDateTime currentTime = ZonedDateTime.now();

        ZonedDateTime firingTime = atZone(
                parseZonedDateTime(
                        broadcastMessageUpdate.getFiringTime(),
                        String.format(
                                "Malformed firing utils, broadcast message id=%d",
                                broadcastMessage.getId()
                        ),
                        Error.BROADCAST_MESSAGE_FIRING_TIME_IS_MALFORMED
                ),
                currentTime.getOffset()
        );

        ZonedDateTime erasingTime;

        if (broadcastMessageUpdate.getFiringTime() != null) {

            if (!firingTime.isAfter(currentTime)) {
                throw new UpdateBroadcastMessageException(
                        String.format(
                                "Firing time=%s is in the past, current time=%s, message id=%d",
                                firingTime,
                                currentTime,
                                broadcastMessage.getId()
                        ),
                        Error.BROADCAST_MESSAGE_FIRING_TIME_IS_IN_PAST
                );

            }

            broadcastMessage.setFiringTime(Timestamp.valueOf(firingTime.toLocalDateTime()));

            if (broadcastMessageUpdate.getErasingTime() != null) {

                erasingTime = atZone(
                        parseZonedDateTime(
                                broadcastMessageUpdate.getErasingTime(),
                                String.format(
                                        "Malformed firing time, broadcast message id=%d",
                                        broadcastMessage.getId()
                                ),
                                Error.BROADCAST_MESSAGE_FIRING_TIME_IS_MALFORMED
                        ),
                        currentTime.getOffset()
                );

                if (!erasingTime.isAfter(firingTime)) {
                    throw new UpdateBroadcastMessageException(
                            String.format(
                                    "Erasing time=%s is before then firing time=%s, message id=%d",
                                    erasingTime,
                                    firingTime,
                                    broadcastMessage.getId()
                            ),
                            Error.BROADCAST_MESSAGE_ERASING_TIME_IS_BEFORE_THEN_FIRING_TIME
                    );
                }

                broadcastMessage.setErasingTime(Timestamp.valueOf(erasingTime.toLocalDateTime()));
            }

            broadcastMessage.setState(BroadcastMessageState.SCHEDULED);
        }
    }
}
