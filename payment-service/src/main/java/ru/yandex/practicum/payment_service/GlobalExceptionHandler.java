package ru.yandex.practicum.payment_service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.yandex.practicum.payment_service.model.exception.BadRequestException;
import ru.yandex.practicum.payment_service.model.exception.ForbiddenException;
import ru.yandex.practicum.payment_service.model.exception.IllegalArgumentException;

import javax.naming.AuthenticationException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;


@ControllerAdvice
public class GlobalExceptionHandler {

    private final String ERROR_CODE = "x-error-code";
    private final String ERROR_MESSAGE = "x-error-message";

    // Обработка 400
    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException e) {
        return errorResponse(e.getErrorCode(), e.getMessage(), BAD_REQUEST);
    }

    @ExceptionHandler({BadRequestException.class})
    public ResponseEntity<?> handleBadRequestException(BadRequestException ex) {
        return errorResponse(ex.getErrorCode(), ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({Exception.class, RuntimeException.class})
    public ResponseEntity<?> handleAnyException(Exception e) {
        return errorResponse(e.getClass().getSimpleName(), e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<?> handleForbiddenException(ForbiddenException e) {
        return errorResponse(e.getClass().getSimpleName(), e.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler({
            AccessDeniedException.class,
            AuthenticationException.class,
            BadCredentialsException.class
    })
    public ResponseEntity<?> authenticationExceptions(Exception e) {
        return errorResponse(e.getClass().getSimpleName(), e.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    private ResponseEntity<Void> errorResponse(String code, String message, HttpStatus status) {
        return ResponseEntity.status(status)
                .header(ERROR_CODE, code)
                .header(ERROR_MESSAGE, message)
                .build();
    }
}
