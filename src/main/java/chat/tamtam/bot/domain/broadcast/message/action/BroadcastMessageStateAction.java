package chat.tamtam.bot.domain.broadcast.message.action;

import java.sql.Timestamp;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import chat.tamtam.bot.domain.broadcast.message.BroadcastMessageEntity;
import chat.tamtam.bot.domain.broadcast.message.BroadcastMessageUpdate;
import chat.tamtam.bot.domain.exception.UpdateBroadcastMessageException;
import chat.tamtam.bot.service.Error;

public abstract class BroadcastMessageStateAction {

    protected static final ZoneOffset SERVER_LOCAL_ZONE_OFFSET = ZoneOffset.of("+03:00"); // [EUROPE/MOSCOW]
    protected static final ZoneId SERVER_LOCAL_ZONE_ID = SERVER_LOCAL_ZONE_OFFSET.normalized();

    public abstract void doAction(
            BroadcastMessageEntity broadcastMessage,
            BroadcastMessageUpdate broadcastMessageUpdate
    );

    protected ZonedDateTime parseZonedDateTime(
            final String zonedDateTime,
            final Error malformedDateTimeError
    ) {
        try {
            return ZonedDateTime.parse(
                    zonedDateTime,
                    DateTimeFormatter.RFC_1123_DATE_TIME
            );
        } catch (DateTimeException ex) {
            throw new UpdateBroadcastMessageException(
                    ex.getLocalizedMessage(),
                    malformedDateTimeError
            );
        }
    }

    protected ZonedDateTime getDateTimeAtLocalZone(
            final String gmtDateTime,
            final Error malformedDateTimeError
    ) {
        ZonedDateTime zonedDateTime = parseZonedDateTime(
                gmtDateTime,
                malformedDateTimeError
        );
        int zoneDif = SERVER_LOCAL_ZONE_OFFSET.getTotalSeconds() - zonedDateTime.getOffset().getTotalSeconds();
        return zonedDateTime.plusSeconds(zoneDif).toLocalDateTime().atZone(SERVER_LOCAL_ZONE_OFFSET);
    }

    protected Timestamp futureTimestamp(
            final ZonedDateTime futureTime,
            final ZonedDateTime pastTime,
            long broadcastMessageId,
            final Error error
    ) {
        if (futureTime.isBefore(pastTime)) {
            throw new UpdateBroadcastMessageException(
                    String.format(
                            "Future time=%s is in the past, past time=%s, message id=%d",
                            futureTime,
                            pastTime,
                            broadcastMessageId
                    ),
                    error
            );
        }
        return Timestamp.valueOf(futureTime.toLocalDateTime());
    }
}
