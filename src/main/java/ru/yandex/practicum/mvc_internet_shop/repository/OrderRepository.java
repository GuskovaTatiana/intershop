package ru.yandex.practicum.mvc_internet_shop.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mvc_internet_shop.model.Order;
import ru.yandex.practicum.mvc_internet_shop.model.enums.OrderStatus;

import java.util.List;

@Repository
public interface OrderRepository extends R2dbcRepository<Order, Integer> {

    @Query("""
            SELECT * FROM t_orders o
            WHERE o.status IN (:status) AND o.deleted = false
            ORDER BY o.id
            """)
    Flux<Order> findByStatusInAndDeletedIsFalse(List<OrderStatus> status);

    @Query("""
            SELECT * FROM t_orders o
            WHERE o.status IN (:status) AND o.deleted = false
            ORDER BY o.id LIMIT 1
            """)
    Mono<Order> findFirstByStatusInAndDeletedIsFalseOrderByCreatedAtDesc(List<OrderStatus> status);

}
