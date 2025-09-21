package ru.yandex.practicum.payment_service.model.exception;

public class IllegalArgumentException extends RuntimeException{

    private String errorCode;
    public IllegalArgumentException(String message) {
        super(message);
    }

    public IllegalArgumentException(String code, String message) {
        super(message);
        this.errorCode = code;
    }
    public IllegalArgumentException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getErrorCode() {
        return errorCode;
    }
}
