package ru.yandex.practicum.payment_service.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class PaymentTransaction {
    private String transactionId;
    private Integer balance;
    private Boolean success;
    private String message;
}
