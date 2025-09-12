package ru.yandex.practicum.payment_service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.yandex.practicum.payment_service.model.exception.BadRequestException;


@ControllerAdvice
public class GlobalExceptionHandler {

    // Обработка 400
    @ExceptionHandler(value = IllegalArgumentException.class)
    public ResponseEntity<Void> handleIllegalArgumentException(IllegalArgumentException ex) {
        return getExceptionResponseEntity(ex, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = BadRequestException.class)
    public ResponseEntity<Void> handleBadRequestException(BadRequestException ex) {
        return getExceptionResponseEntity(ex, HttpStatus.BAD_REQUEST);
    }

    private static ResponseEntity<Void> getExceptionResponseEntity(Exception ex, HttpStatus status) {
        return ResponseEntity.status(status)
                .header("x-error-message", ex.getMessage())
                .build();
    }

}
