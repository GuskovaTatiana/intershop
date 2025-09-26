package ru.yandex.practicum.payment_service.model.exception;

public class ForbiddenException extends RuntimeException{

    private String errorCode;

    public ForbiddenException() {
    }

    public ForbiddenException(String message) {
        super(message);
    }

    public ForbiddenException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
