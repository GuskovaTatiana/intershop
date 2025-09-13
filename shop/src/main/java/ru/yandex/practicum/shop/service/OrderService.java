package ru.yandex.practicum.shop.service;

import reactor.core.publisher.Mono;
import ru.yandex.practicum.shop.model.dto.OrderDTO;

public interface OrderService {

    Mono<OrderDTO> getOrderInCart();
}
