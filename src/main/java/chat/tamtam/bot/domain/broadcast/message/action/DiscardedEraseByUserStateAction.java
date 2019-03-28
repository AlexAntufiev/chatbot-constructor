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
public final class DiscardedEraseByUserStateAction extends BroadcastMessageStateAction {
    @Override
    protected void setText(
            final BroadcastMessageEntity broadcastMessage,
            final BroadcastMessageUpdate broadcastMessageUpdate
    ) {
        rejectTextUpdate(broadcastMessage, broadcastMessageUpdate);
    }

    @Override
    public void doAction(
            final BroadcastMessageEntity broadcastMessage,
            final BroadcastMessageUpdate broadcastMessageUpdate
    ) {
        updateText(broadcastMessage, broadcastMessageUpdate);

        if (broadcastMessageUpdate.getErasingTime() == null) {
            return;
        }

        ZonedDateTime erasingTime = getDateTimeAtLocalZone(
                broadcastMessageUpdate.getErasingTime(),
                Error.BROADCAST_MESSAGE_ERASING_TIME_IS_MALFORMED
        );

        ZonedDateTime currentTime = ZonedDateTime.now(SERVER_LOCAL_ZONE_ID);

        broadcastMessage.setErasingTime(
                futureTimestamp(
                        erasingTime,
                        currentTime,
                        broadcastMessage.getId(),
                        Error.BROADCAST_MESSAGE_ERASING_TIME_IS_IN_THE_PAST
                )
        );

        broadcastMessage.setState(BroadcastMessageState.SENT);
    }
}