package chat.tamtam.bot.domain.broadcast.message.action;

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
public final class ScheduledStateAction extends BroadcastMessageStateAction {
    @Override
    protected void setText(
            final BroadcastMessageEntity broadcastMessage,
            final BroadcastMessageUpdate broadcastMessageUpdate
    ) {
        if (broadcastMessageUpdate.getText().isEmpty()) {
            throw new UpdateBroadcastMessageException(
                    String.format(
                            "Can't update broadcastMessage's text with id=%d because text is empty",
                            broadcastMessage.getId()
                    ),
                    Error.BROADCAST_MESSAGE_TEXT_IS_EMPTY
            );
        }
        broadcastMessage.setText(broadcastMessageUpdate.getText());
    }

    @Override
    public void doAction(
            final BroadcastMessageEntity broadcastMessage,
            final BroadcastMessageUpdate broadcastMessageUpdate
    ) {
        updateText(broadcastMessage, broadcastMessageUpdate);

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

        ZonedDateTime currentTime = ZonedDateTime.now(SERVER_LOCAL_ZONE_ID);

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
