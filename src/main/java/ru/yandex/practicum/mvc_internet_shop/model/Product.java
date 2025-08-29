package ru.yandex.practicum.mvc_internet_shop.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "t_products")
@Getter
@Setter
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Integer id;
    @Column(name = "title", nullable = false)
    private String title;
    @Column(name = "image_url")
    private String imageUrl;
    @Column(name = "description")
    private String description;
    @Column(name = "price")
    private Integer price;

}
