package ru.yandex.practicum.mvc_internet_shop.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import ru.yandex.practicum.mvc_internet_shop.model.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "t_orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "t_orders_id_seq")
    @SequenceGenerator(name = "t_orders_id_seq", sequenceName = "t_orders_id_seq", allocationSize = 1)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status = OrderStatus.CREATE;

    @OneToMany(mappedBy = "orderId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductsInOrder> products = new ArrayList<>();

    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}
