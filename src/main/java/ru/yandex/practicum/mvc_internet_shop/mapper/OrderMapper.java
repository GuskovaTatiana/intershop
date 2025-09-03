package ru.yandex.practicum.mvc_internet_shop.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.mvc_internet_shop.model.Order;
import ru.yandex.practicum.mvc_internet_shop.model.dto.OrderDTO;

import java.util.List;

@Component
@AllArgsConstructor
public class OrderMapper {
    private final ProductMapper productMapper;

    public OrderDTO toDto(Order empty) {
        return OrderDTO.builder()
                .id(empty.getId())
                .status(empty.getStatus())
                .products(empty.getProducts().stream().map(it -> productMapper.toDto(it.getProduct(), it.getProductCount(), it.getId())).toList())
                .build();
    }

    public List<OrderDTO> toDto(List<Order> orders) {
        return orders.stream().map(this::toDto).toList();
    }
}
