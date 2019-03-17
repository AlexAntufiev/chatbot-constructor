package chat.tamtam.bot.configuration.handler;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import chat.tamtam.bot.domain.exception.ChannelStoreException;
import chat.tamtam.bot.domain.exception.NotFoundEntityException;
import chat.tamtam.bot.domain.exception.TamBotException;
import chat.tamtam.bot.domain.response.FailResponse;
import chat.tamtam.bot.domain.response.TamBotChannelsFailResponse;

@RestControllerAdvice
public class ControllerHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(NotFoundEntityException.class)
    public ResponseEntity<Object> handleBadRequest(Exception ex, WebRequest request) {
        return handleExceptionInternal(ex, ex.getLocalizedMessage(), new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }
    @ExceptionHandler(TamBotException.class)
    public ResponseEntity<Object> handleBotSubscriptionException(TamBotException ex, WebRequest request) {
        return handleExceptionInternal(
                ex,
                new FailResponse(ex.getErrorKey()),
                new HttpHeaders(),
                HttpStatus.OK,
                request
        );
    }

    public ResponseEntity<Object> handleChannelsStoreException(ChannelStoreException ex, WebRequest request) {
        return handleExceptionInternal(
                ex,
                new TamBotChannelsFailResponse(ex.getErrorKey(), ex.getChannels()),
                new HttpHeaders(),
                HttpStatus.OK,
                request
        );
    }
}
