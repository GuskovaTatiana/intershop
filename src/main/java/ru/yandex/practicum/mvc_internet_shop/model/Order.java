package ru.yandex.practicum.mvc_internet_shop.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import ru.yandex.practicum.mvc_internet_shop.model.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Table(name = "t_orders")
public class Order {

    @Id
    private Integer id;
    private OrderStatus status = OrderStatus.CREATE;

    @Transient
    private List<ProductsInOrder> products = new ArrayList<>();

    private Boolean deleted = false;
    @Column("created_at")
    private LocalDateTime createdAt = LocalDateTime.now();;
    @Column("updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}
