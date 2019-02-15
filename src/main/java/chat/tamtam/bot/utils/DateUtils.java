package chat.tamtam.bot.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatterBuilder;

import lombok.experimental.UtilityClass;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_TIME;

@UtilityClass
public class DateUtils {

    public String instantToString(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneId.of("Europe/Moscow"))
                .format(new DateTimeFormatterBuilder().parseCaseInsensitive()
                        .append(ISO_LOCAL_DATE)
                        .appendLiteral(' ')
                        .append(ISO_LOCAL_TIME)
                        .toFormatter());
    }
}
