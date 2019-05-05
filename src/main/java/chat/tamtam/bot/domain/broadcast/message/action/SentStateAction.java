package chat.tamtam.bot.domain.broadcast.message.action;

import java.time.Instant;

import org.springframework.stereotype.Component;

import chat.tamtam.bot.domain.broadcast.message.BroadcastMessage;
import chat.tamtam.bot.domain.broadcast.message.BroadcastMessageState;
import chat.tamtam.bot.domain.broadcast.message.BroadcastMessageUpdate;
import chat.tamtam.bot.service.Error;
import lombok.NoArgsConstructor;

@Component
@NoArgsConstructor
public final class SentStateAction extends BroadcastMessageStateAction {
    @Override
    protected void setText(
            final BroadcastMessage broadcastMessage,
            final BroadcastMessageUpdate broadcastMessageUpdate
    ) {
        rejectTextUpdate(broadcastMessage, broadcastMessageUpdate);
    }

    @Override
    public void doAction(
            final BroadcastMessage broadcastMessage,
            final BroadcastMessageUpdate broadcastMessageUpdate
    ) {
        updateText(broadcastMessage, broadcastMessageUpdate);

        if (broadcastMessageUpdate.getErasingTime() == null) {
            broadcastMessage.setErasingTime(null);
            broadcastMessage.setState(BroadcastMessageState.DISCARDED_ERASE_BY_USER);
            return;
        }

        Instant currentTime = Instant.now();

        broadcastMessage.setErasingTime(
                getInstant(
                        broadcastMessageUpdate.getErasingTime(),
                        currentTime,
                        broadcastMessage.getId(),
                        Error.BROADCAST_MESSAGE_ERASING_TIME_IS_MALFORMED,
                        Error.BROADCAST_MESSAGE_ERASING_TIME_IS_IN_THE_PAST
                )
        );
    }
}
