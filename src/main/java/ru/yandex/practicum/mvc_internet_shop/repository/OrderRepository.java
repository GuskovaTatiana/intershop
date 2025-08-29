package ru.yandex.practicum.mvc_internet_shop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.mvc_internet_shop.model.Order;
import ru.yandex.practicum.mvc_internet_shop.model.enums.OrderStatus;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.products pr WHERE o.status IN :status AND o.deleted = false ORDER BY o.createdAt DESC")
    List<Order> findByStatusInAndDeletedIsFalseOrderByCreatedAtDesc(List<OrderStatus> status);

}
