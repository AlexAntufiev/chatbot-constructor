package chat.tamtam.bot.domain.broadcast.message.action;

import java.time.Instant;

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
    protected void setText(
            final BroadcastMessageEntity broadcastMessage,
            final BroadcastMessageUpdate broadcastMessageUpdate
    ) {
        applyTextUpdate(broadcastMessage, broadcastMessageUpdate);
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

        Instant currentTime = Instant.now();

        broadcastMessage.setFiringTime(
                getInstant(
                        broadcastMessageUpdate.getFiringTime(),
                        currentTime,
                        broadcastMessage.getId(),
                        Error.BROADCAST_MESSAGE_FIRING_TIME_IS_MALFORMED,
                        Error.BROADCAST_MESSAGE_FIRING_TIME_IS_IN_PAST
                )
        );

        if (broadcastMessageUpdate.getErasingTime() == null) {
            broadcastMessage.setErasingTime(null);
            return;
        }

        broadcastMessage.setErasingTime(
                getInstant(
                        broadcastMessageUpdate.getErasingTime(),
                        broadcastMessage.getFiringTime(),
                        broadcastMessage.getId(),
                        Error.BROADCAST_MESSAGE_ERASING_TIME_IS_MALFORMED,
                        Error.BROADCAST_MESSAGE_ERASING_TIME_IS_BEFORE_THEN_FIRING_TIME
                )
        );
    }
}
