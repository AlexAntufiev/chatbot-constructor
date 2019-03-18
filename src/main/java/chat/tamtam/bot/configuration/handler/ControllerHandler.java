package chat.tamtam.bot.configuration.handler;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import chat.tamtam.bot.domain.exception.ChatChannelStoreException;
import chat.tamtam.bot.domain.exception.NotFoundEntityException;
import chat.tamtam.bot.domain.exception.TamBotException;
import chat.tamtam.bot.domain.response.ChatChannelFailResponse;
import chat.tamtam.bot.domain.response.FailResponse;
import io.micrometer.core.lang.Nullable;

@RestControllerAdvice
public class ControllerHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(NotFoundEntityException.class)
    public ResponseEntity<Object> handleBadRequest(NotFoundEntityException ex, WebRequest request) {
        return handle(
                ex,
                new FailResponse(ex.getErrorKey()),
                request
        );
    }

    @ExceptionHandler(TamBotException.class)
    public ResponseEntity<Object> handleBotSubscriptionException(TamBotException ex, WebRequest request) {
        return handle(
                ex,
                new FailResponse(ex.getErrorKey()),
                request
        );
    }

    @ExceptionHandler(ChatChannelStoreException.class)
    public ResponseEntity<Object> handleChatChannelStoreException(ChatChannelStoreException ex, WebRequest request) {
        return handle(
                ex,
                new ChatChannelFailResponse(ex.getErrorKey(), ex.getChatChannel()),
                request
        );
    }

    private ResponseEntity<Object> handle(final Exception ex, @Nullable final Object body, final WebRequest request) {
        return handleExceptionInternal(
                ex,
                body,
                new HttpHeaders(),
                HttpStatus.OK,
                request
        );
    }
}
