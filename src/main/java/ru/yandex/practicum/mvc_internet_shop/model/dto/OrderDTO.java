package ru.yandex.practicum.mvc_internet_shop.model.dto;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.yandex.practicum.mvc_internet_shop.model.ProductsInOrder;
import ru.yandex.practicum.mvc_internet_shop.model.enums.OrderStatus;

import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@Setter
public class OrderDTO {
    private Integer id;
    private OrderStatus status;
    private List<ProductDTO> products;

    public Integer getTotalPrice() {
        int totalPrice = 0;
        if (products != null) {
            for (ProductDTO prod : products) {
                totalPrice += prod.getTotalPrice();
            }
        }
        return totalPrice;
    }

    public String geStatusName() {
        if (status.equals(OrderStatus.CREATE)) {
            return "Формируется";
        } else if (status.equals(OrderStatus.IN_PROGRESS)) {
            return "В обработке";
        } else if (status.equals(OrderStatus.CLOSED)) {
            return "Завершен";
        } else {
            return "Завершен";
        }
    }
}
