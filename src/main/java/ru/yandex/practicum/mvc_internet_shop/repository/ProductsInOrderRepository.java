package ru.yandex.practicum.mvc_internet_shop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.mvc_internet_shop.model.ProductsInOrder;

@Repository
public interface ProductsInOrderRepository extends JpaRepository<ProductsInOrder, Integer> {
}
