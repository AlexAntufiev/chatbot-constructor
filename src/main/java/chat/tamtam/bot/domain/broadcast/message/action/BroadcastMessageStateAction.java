package chat.tamtam.bot.domain.broadcast.message.action;

import java.time.DateTimeException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import chat.tamtam.bot.domain.broadcast.message.BroadcastMessageEntity;
import chat.tamtam.bot.domain.broadcast.message.BroadcastMessageUpdate;
import chat.tamtam.bot.domain.exception.UpdateBroadcastMessageException;
import chat.tamtam.bot.service.Error;

public abstract class BroadcastMessageStateAction {

    public abstract void doAction(
            BroadcastMessageEntity broadcastMessage,
            BroadcastMessageUpdate broadcastMessageUpdate
    );

    protected ZonedDateTime parseZonedDateTime(
            final String zonedDateTime,
            final String errorMessage,
            final Error error
    ) {
        try {
            return ZonedDateTime.parse(
                    zonedDateTime,
                    DateTimeFormatter.RFC_1123_DATE_TIME
            );
        } catch (DateTimeException ex) {
            throw new UpdateBroadcastMessageException(
                    errorMessage + "\n" + ex.getLocalizedMessage(),
                    error
            );
        }
    }

    protected ZonedDateTime atZone(
            final ZonedDateTime futureDateTime,
            final ZoneOffset zoneOffset
    ) {
        int zoneDif = zoneOffset.getTotalSeconds() - futureDateTime.getOffset().getTotalSeconds();
        return futureDateTime.plusSeconds(zoneDif).toLocalDateTime().atZone(zoneOffset);
    }
}
