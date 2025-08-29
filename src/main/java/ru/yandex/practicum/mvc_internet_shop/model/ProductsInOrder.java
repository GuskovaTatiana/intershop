package ru.yandex.practicum.mvc_internet_shop.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Entity
@Table(name = "t_products_in_order")
public class ProductsInOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "t_products_in_order_id_seq")
    @SequenceGenerator(name = "t_products_in_order_id_seq", sequenceName = "t_products_in_order_id_seq", allocationSize = 1)
    private Integer id;

    @Column(name = "order_id")
    private Integer orderId;

    @Column(name = "product_id")
    private Integer productId;

    @ManyToOne
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;

    @Column(name = "product_count")
    private Integer productCount;

}
