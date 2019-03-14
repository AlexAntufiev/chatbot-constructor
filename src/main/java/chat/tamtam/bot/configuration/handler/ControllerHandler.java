package chat.tamtam.bot.configuration.handler;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import chat.tamtam.bot.domain.exception.NotFoundEntityException;
import chat.tamtam.bot.domain.exception.TamBotSubscriptionException;
import chat.tamtam.bot.domain.response.BotSubscriptionFailEntity;

@RestControllerAdvice
public class ControllerHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(NotFoundEntityException.class)
    public ResponseEntity<Object> handleBadRequest(Exception ex, WebRequest request) {
        return handleExceptionInternal(ex, ex.getLocalizedMessage(), new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }
    @ExceptionHandler(TamBotSubscriptionException.class)
    public ResponseEntity<Object> handleBotSubscriptionException(TamBotSubscriptionException ex, WebRequest request) {
        return handleExceptionInternal(
                ex,
                new BotSubscriptionFailEntity(ex.getErrorKey()),
                new HttpHeaders(),
                HttpStatus.OK,
                request
        );
    }
}
