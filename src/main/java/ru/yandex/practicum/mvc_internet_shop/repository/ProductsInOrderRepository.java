package ru.yandex.practicum.mvc_internet_shop.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mvc_internet_shop.model.ProductsInOrder;

@Repository
public interface ProductsInOrderRepository extends R2dbcRepository<ProductsInOrder, Integer> {

    Mono<ProductsInOrder> findFirstByOrderIdAndProductId(Integer orderId, Integer productId);

    Flux<ProductsInOrder> findAllByOrderId(Integer orderId);
}
