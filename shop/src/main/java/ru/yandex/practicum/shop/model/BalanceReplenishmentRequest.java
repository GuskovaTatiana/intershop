package ru.yandex.practicum.shop.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class BalanceReplenishmentRequest {
    @Min(1)
    @Max(100000)
    private Integer amount;
}
