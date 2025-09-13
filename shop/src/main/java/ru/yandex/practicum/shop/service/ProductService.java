package ru.yandex.practicum.shop.service;

import reactor.core.publisher.Mono;
import ru.yandex.practicum.shop.model.Product;

import java.util.List;

public interface ProductService {

    Mono<List<Product>> getAllProducts();
}
