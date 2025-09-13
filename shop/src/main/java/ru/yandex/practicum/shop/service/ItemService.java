package ru.yandex.practicum.shop.service;

import reactor.core.publisher.Mono;
import ru.yandex.practicum.shop.model.ProductsInOrder;
import ru.yandex.practicum.shop.model.dto.ProductDTO;

import java.util.List;


public interface ItemService {

    Mono<List<ProductsInOrder>> setProductInItem(List<ProductsInOrder> productsInOrders);

    Mono<List<ProductDTO>> getProductsFromCart();

    Mono<ProductsInOrder> findByOrderIdAndProductId(Integer productId);
}
