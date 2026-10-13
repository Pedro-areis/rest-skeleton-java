package dev.pedroreis.rest_skeleton_java.user.adapters.inbound;

import dev.pedroreis.rest_skeleton_java.shared.web.ErrorResponse;
import dev.pedroreis.rest_skeleton_java.user.domain.exception.EmailAlreadyExistsException;
import dev.pedroreis.rest_skeleton_java.user.domain.exception.InvalidBirthDateException;
import dev.pedroreis.rest_skeleton_java.user.domain.exception.InvalidEmailException;
import dev.pedroreis.rest_skeleton_java.user.domain.exception.InvalidPasswordException;
import dev.pedroreis.rest_skeleton_java.user.domain.exception.UserNotFoundException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Traduz as exceções de negócio de {@code user} para HTTP. O restante (validação, catch-all)
 * é do {@code GlobalExceptionHandler} em {@code shared/web} (ver ADR 005).
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class UserExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        return ErrorResponse.entity(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        return ErrorResponse.entity(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(InvalidEmailException.class)
    public ResponseEntity<ErrorResponse> handleInvalidEmail(InvalidEmailException ex) {
        return ErrorResponse.entity(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(InvalidBirthDateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidBirthDate(InvalidBirthDateException ex) {
        return ErrorResponse.entity(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPassword(InvalidPasswordException ex) {
        return ErrorResponse.entity(HttpStatus.BAD_REQUEST, ex.getMessage());
    }
}
