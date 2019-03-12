package chat.tamtam.bot.configuration.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import chat.tamtam.bot.domain.exception.NoFoundEntityException;

@RestControllerAdvice
public class ControllerHandler {

    @ExceptionHandler(NoFoundEntityException.class)
    public ResponseEntity<Object> handleBadRequest(Exception ex, WebRequest request) {
        return new ResponseEntity<>(ex.getLocalizedMessage(), HttpStatus.NOT_FOUND);
    }
}
