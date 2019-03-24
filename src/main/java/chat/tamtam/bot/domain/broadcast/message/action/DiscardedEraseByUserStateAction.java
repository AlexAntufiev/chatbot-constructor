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
public final class DiscardedEraseByUserStateAction extends BroadcastMessageStateAction {
    @Override
    public void doAction(
            final BroadcastMessageEntity broadcastMessage,
            final BroadcastMessageUpdate broadcastMessageUpdate
    ) {
        if (broadcastMessageUpdate.getErasingTime() == null) {
            throw new UpdateBroadcastMessageException(
                    String.format(
                            "Broadcast message with id=%d has discarded erasing utils already",
                            broadcastMessage.getId()
                    ),
                    Error.BROADCAST_MESSAGE_ERASE_ALREADY_DISCARDED
            );
        }

        ZonedDateTime currentTime = ZonedDateTime.now();

        ZonedDateTime erasingTime = atZone(
                parseZonedDateTime(
                        broadcastMessageUpdate.getErasingTime(),
                        String.format(
                                "Erasing time is malformed, broadcast message id=%d",
                                broadcastMessage.getId()
                        ),
                        Error.BROADCAST_MESSAGE_ERASING_TIME_IS_MALFORMED
                ),
                currentTime.getOffset()
        );

        if (!erasingTime.isAfter(currentTime)) {
            throw new UpdateBroadcastMessageException(
                    String.format(
                            "Erasing time=%s is in the past, current time=%s, message id=%d",
                            erasingTime,
                            currentTime,
                            broadcastMessage.getId()
                    ),
                    Error.BROADCAST_MESSAGE_ERASING_TIME_IS_IN_THE_PAST
            );
        }

        broadcastMessage.setErasingTime(Timestamp.valueOf(erasingTime.toLocalDateTime()));
        broadcastMessage.setState(BroadcastMessageState.SENT);
    }
}
