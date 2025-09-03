package ru.yandex.practicum.mvc_internet_shop.model;


import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "t_products")
@Getter
@Setter
public class Product {
    @Id
    private Integer id;

    private String title;
    @Column("image_url")
    private String imageUrl;

    private String description;

    private Integer price;

}
