package chat.tamtam.bot.domain.broadcast.message.action;

import java.time.Instant;
import java.time.ZonedDateTime;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import chat.tamtam.bot.domain.broadcast.message.BroadcastMessageEntity;
import chat.tamtam.bot.domain.broadcast.message.BroadcastMessageState;
import chat.tamtam.bot.domain.broadcast.message.BroadcastMessageUpdate;
import chat.tamtam.bot.domain.exception.UpdateBroadcastMessageException;
import chat.tamtam.bot.service.Error;
import lombok.NoArgsConstructor;

@Component
@NoArgsConstructor
public final class CreatedStateAction extends BroadcastMessageStateAction {
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

        if (broadcastMessageUpdate.getFiringTime() != null) {

            if (StringUtils.isEmpty(broadcastMessage.getText())) {
                throw new UpdateBroadcastMessageException(
                        String.format(
                                "Can't schedule messageOf with id=%d, because text is empty",
                                broadcastMessage.getId()
                        ),
                        Error.BROADCAST_MESSAGE_TEXT_IS_EMPTY
                );
            }

            Instant currentTime = Instant.now();

            ZonedDateTime erasingTime;

            broadcastMessage.setFiringTime(
                    getInstant(
                            broadcastMessageUpdate.getFiringTime(),
                            currentTime,
                            broadcastMessage.getId(),
                            Error.BROADCAST_MESSAGE_FIRING_TIME_IS_MALFORMED,
                            Error.BROADCAST_MESSAGE_FIRING_TIME_IS_IN_PAST
                    )
            );

            if (broadcastMessageUpdate.getErasingTime() != null) {
                broadcastMessage.setErasingTime(
                        getInstant(
                                broadcastMessageUpdate.getErasingTime(),
                                broadcastMessage.getFiringTime(),
                                broadcastMessage.getId(),
                                Error.BROADCAST_MESSAGE_FIRING_TIME_IS_MALFORMED,
                                Error.BROADCAST_MESSAGE_ERASING_TIME_IS_BEFORE_THEN_FIRING_TIME
                        )
                );
            }

            broadcastMessage.setState(BroadcastMessageState.SCHEDULED);
        }
    }
}
