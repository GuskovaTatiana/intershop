package ru.yandex.practicum.payment_service.model.exception;


public class BadRequestException extends RuntimeException {

    private String errorCode;
    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String code, String message) {
        super(message);
        this.errorCode = code;
    }
    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getErrorCode() {
        return errorCode;
    }

}