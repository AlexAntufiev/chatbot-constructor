package chat.tamtam.bot.domain.broadcast.message.utils;

import java.sql.Timestamp;
import java.time.DateTimeException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import chat.tamtam.bot.domain.exception.CreateBroadcastMessageException;
import chat.tamtam.bot.service.Error;

public final class TimeUtils {
    private TimeUtils() { }

    public static ZonedDateTime parseZonedDateTime(
            final String zonedDateTime,
            final String errorMessage,
            final Error error
    ) {
        try {
            return ZonedDateTime.parse(
                    zonedDateTime,
                    DateTimeFormatter.RFC_1123_DATE_TIME
            );
        } catch (DateTimeException dTE) {
            throw new CreateBroadcastMessageException(
                    errorMessage,
                    error
            );
        }
    }

    public static Timestamp getTimestamp(
            final ZonedDateTime futureDateTime,
            final ZonedDateTime currentDateTime,
            final String baseTimeAfterSuppliedTimeMessageFormat,
            final Error baseTimeAfterSuppliedTimeError
    ) {
        // @todo #CC-63 Add value to props that will be minimal diff between current-utils and future-utils
        int zoneDif = currentDateTime.getOffset().getTotalSeconds() - futureDateTime.getOffset().getTotalSeconds();
        Timestamp futureTimestamp = Timestamp.valueOf(futureDateTime.plusSeconds(zoneDif).toLocalDateTime());
        Timestamp currentTimestamp = Timestamp.valueOf(currentDateTime.toLocalDateTime());
        if (!futureTimestamp.after(currentTimestamp)) {
            throw new CreateBroadcastMessageException(
                    String.format(
                            baseTimeAfterSuppliedTimeMessageFormat,
                            futureTimestamp.getTime(),
                            currentTimestamp.getTime()
                    ),
                    baseTimeAfterSuppliedTimeError
            );
        }
        return futureTimestamp;
    }
}
