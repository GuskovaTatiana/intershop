package ru.yandex.practicum.mvc_internet_shop.controller;


import org.hibernate.exception.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.yandex.practicum.mvc_internet_shop.model.exception.BadRequestException;


@ControllerAdvice
public class GlobalExceptionHandler {

    // Обработка 400
    @ExceptionHandler({IllegalArgumentException.class, ConstraintViolationException.class, BadRequestException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleIllegalArgumentException() {
        return "bad-request.html";
    }


}
