package chat.tamtam.bot.configuration.logging;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.CodeSignature;
import org.springframework.context.annotation.Configuration;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;

@Slf4j
@Aspect
@Configuration
public class AopLogger {

    private static final String[] TIME_UNITS = {"ns", "μs", "ms", "s"};

    @Around("@annotation(org.springframework.web.bind.annotation.RequestMapping)"
            + "||@annotation(org.springframework.web.bind.annotation.PostMapping)"
            + "||@annotation(org.springframework.web.bind.annotation.GetMapping)"
            + "||@annotation(org.springframework.web.bind.annotation.DeleteMapping)"
            + "||@annotation(org.springframework.web.bind.annotation.PutMapping)"
            + "||@annotation(org.springframework.web.bind.annotation.PatchMapping)"
            + "||@annotation(org.springframework.web.bind.annotation.ExceptionHandler)"
            + "||@annotation(org.springframework.scheduling.annotation.Scheduled)"
            + "||@annotation(org.springframework.scheduling.annotation.Async)"
            + "||@annotation(chat.tamtam.bot.configuration.logging.Loggable)"
    )
    @SneakyThrows
    public Object around(ProceedingJoinPoint joinPoint) {
        if (!log.isDebugEnabled()) {
            return joinPoint.proceed(joinPoint.getArgs());
        }

        LocalDateTime evaluationStartTime = LocalDateTime.now();
        log.debug(
                "Entered  method '{}' with arguments [{}] at {}",
                joinPoint.getSignature().getName(),
                getCallArgumentsAsString(joinPoint),
                ISO_DATE_TIME.format(evaluationStartTime)
        );

        Object evaluationResult;
        try {
            evaluationResult = joinPoint.proceed(joinPoint.getArgs());
        } catch (Throwable e) {
            LocalDateTime evaluationEndTime = LocalDateTime.now();
            log.debug(
                    "Exited method '{}' with exception [{}] at {} ({} elapsed)",
                    joinPoint.getSignature().getName(),
                    e.getLocalizedMessage(),
                    ISO_DATE_TIME.format(evaluationEndTime),
                    formatDurationBetween(evaluationStartTime, evaluationEndTime)
            );
            throw e;
        }

        LocalDateTime evaluationEndTime = LocalDateTime.now();
        log.debug(
                "Exited method '{}' with result [{}] at {} ({} elapsed)",
                joinPoint.getSignature().getName(),
                evaluationResult,
                ISO_DATE_TIME.format(evaluationEndTime),
                formatDurationBetween(evaluationStartTime, evaluationEndTime)
        );

        return evaluationResult;
    }

    private static String getCallArgumentsAsString(JoinPoint point) {
        CodeSignature methodSignature = (CodeSignature) point.getSignature();
        String[] joinPointMethodNames = methodSignature.getParameterNames();

        Object[] joinPointMethodArguments = point.getArgs();

        return IntStream.range(0, joinPointMethodNames.length).boxed()
                .map(i -> joinPointMethodNames[i] + " = " + joinPointMethodArguments[i])
                .collect(Collectors.joining(", "));
    }

    private static String formatDurationBetween(long duration, long subDuration, int timeUnitIndex) {
        final int border = 1_000;
        if (timeUnitIndex < TIME_UNITS.length && duration >= border) {
            return formatDurationBetween(
                    duration / border,
                    duration % border,
                    timeUnitIndex + 1
            );
        } else {
            return String.format("%d.%03d %s", duration, subDuration, TIME_UNITS[timeUnitIndex]);
        }
    }

    private static String formatDurationBetween(Temporal startTime, Temporal endTime) {
        return formatDurationBetween(Duration.between(startTime, endTime).toNanos(), 0, 0);
    }
}
