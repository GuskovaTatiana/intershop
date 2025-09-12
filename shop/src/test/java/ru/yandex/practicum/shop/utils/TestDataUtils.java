package ru.yandex.practicum.shop.utils;


import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.model.PaymentResponse;
import ru.yandex.practicum.shop.model.dto.OrderDTO;
import ru.yandex.practicum.shop.model.dto.ProductDTO;
import ru.yandex.practicum.shop.model.enums.OrderStatus;

import java.util.ArrayList;
import java.util.List;

@Component
@NoArgsConstructor
public class TestDataUtils {

    public Page<ProductDTO> getListProduct() {
        List<ProductDTO> dtos = new ArrayList<>();
        dtos.add(getProduct(1, "Товар 1", "/images/image1.png", "Описание 1", 3));
        dtos.add(getProduct(2, "Товар 2", "/images/image2.png", "Описание 2", 65));
        dtos.add(getProduct(3, "Товар 3", "/images/image3.png", "Описание 3", 25));
        return new PageImpl<>(dtos, PageRequest.of(0, 10), 20);
    }

    public ProductDTO getProduct(Integer id, String title, String imageUrl, String description, Integer price) {
        return ProductDTO.builder()
                .id(id)
                .title(title)
                .imageUrl(imageUrl)
                .description(description)
                .price(price)
                .count(id)
                .itemId(id)
                .build();
    }

    public List<OrderDTO> getListOrder() {
        List<OrderDTO> dto = new ArrayList<>();
        dto.add(getOrder(1, OrderStatus.CLOSED, getListProduct().getContent()));
        dto.add(getOrder(2, OrderStatus.IN_PROGRESS, List.of(getProduct(1, "Товар 1", "/images/image1.png", "Описание 1", 3))));
        dto.add(getOrder(3, OrderStatus.CREATE, null));
        return dto;
    }

    public OrderDTO getOrder(Integer id, OrderStatus status, List<ProductDTO> products) {
        return OrderDTO.builder()
                .id(id)
                .status(status)
                .products(products)
                .build();
    }

    public PaymentResponse getPaymentResponse(String message) {
        PaymentResponse response = new PaymentResponse();
        response.setSuccess(true);
        response.setTransactionId("8b15d1ae-48a7-499c-b8c8-37c278158aa5");
        response.setMessage(message);

        return response;
    }

}
