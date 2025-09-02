package ru.yandex.practicum.mvc_internet_shop.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;


@Getter
@Setter
@Table(name = "t_products_in_order")
public class ProductsInOrder {

    @Id
    private Integer id;

    @Column("order_id")
    private Integer orderId;

    @Column("product_id")
    private Integer productId;

    @Transient
    private Product product;

    @Column("product_count")
    private Integer productCount;

}
