package ru.yandex.practicum.mvc_internet_shop.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Setter
@Getter
public class ProductDTO {
    private Integer id;
    private String title;
    private String imageUrl;
    private String description;
    private Integer price;
    private Integer count;
    private Integer itemId;

    public Integer getTotalPrice() {
        return price * count;
    }
}
