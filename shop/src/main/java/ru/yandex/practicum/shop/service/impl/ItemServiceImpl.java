package ru.yandex.practicum.shop.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.shop.model.Product;
import ru.yandex.practicum.shop.model.ProductsInOrder;
import ru.yandex.practicum.shop.model.dto.ProductDTO;
import ru.yandex.practicum.shop.repository.ProductsInOrderRepository;
import ru.yandex.practicum.shop.service.ItemService;
import ru.yandex.practicum.shop.service.OrderService;
import ru.yandex.practicum.shop.service.ProductService;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {

    private ProductService productService;
    private OrderService orderService;
    private final ProductsInOrderRepository productsInOrderRepository;

    @Autowired
    public ItemServiceImpl(@Lazy ProductService productService, @Lazy OrderService orderService, ProductsInOrderRepository productsInOrderRepository) {
        this.productService = productService;
        this.orderService = orderService;
        this.productsInOrderRepository = productsInOrderRepository;
    }

    /**
     * Фильтрация по идентификаторам и сортировка списка продуктов
     */
    @Override
    public Mono<List<ProductsInOrder>> setProductInItem(List<ProductsInOrder> productsInOrders) {

        return productService.getAllProducts()
                .flatMap(products -> {
                    List<Integer> productIds = productsInOrders.stream().map(ProductsInOrder::getProductId).toList();
                    Map<Integer, ProductsInOrder> mapProductsInOrders = productsInOrders.stream()
                            .collect(Collectors.toMap(
                                    ProductsInOrder::getProductId,
                                    Function.identity(),
                                    (existing, replacement) -> existing // обработка дубликатов - берем существующий
                            ));
                    // Фильтрация продуктов по идентификаторам
                    List<Product> filteredProducts = products.stream()
                            .filter(it -> productIds.contains(it.getId()))
                            .toList();
                    // записываем продукты в итемсы
                    filteredProducts.forEach(it -> mapProductsInOrders.get(it.getId()).setProduct(it));

                    // Сортировка и возврат результата
                    return Mono.just(mapProductsInOrders.values().stream()
                            .sorted(Comparator.comparing(ProductsInOrder::getProductId)).toList());
                });
    }

    /**
     * Получение списка продуктов в корзине
     */
    @Override
    public Mono<List<ProductDTO>> getProductsFromCart() {

        return orderService.getOrderInCart()
                .flatMap(order -> {
                    // Сортировка и возврат результата
                    return Mono.just(order.getProducts());
                });
    }

    /**
     * Получение объекта по Id заказа и Id товара
     * */
    @Override
    public Mono<ProductsInOrder> findByOrderIdAndProductId(Integer productId) {
        return orderService.getOrderInCart()
                .flatMap(order -> {
                    // Сортировка и возврат результата
                    return productsInOrderRepository.findFirstByOrderIdAndProductId(order.getId(), productId).switchIfEmpty( Mono.empty());
                });
    }
}
