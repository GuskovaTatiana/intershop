package ru.yandex.practicum.mvc_internet_shop.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mvc_internet_shop.mapper.OrderMapper;
import ru.yandex.practicum.mvc_internet_shop.model.Order;
import ru.yandex.practicum.mvc_internet_shop.model.ProductsInOrder;
import ru.yandex.practicum.mvc_internet_shop.model.dto.OrderDTO;
import ru.yandex.practicum.mvc_internet_shop.model.enums.OrderStatus;
import ru.yandex.practicum.mvc_internet_shop.model.exception.BadRequestException;
import ru.yandex.practicum.mvc_internet_shop.repository.OrderRepository;
import ru.yandex.practicum.mvc_internet_shop.repository.ProductRepository;
import ru.yandex.practicum.mvc_internet_shop.repository.ProductsInOrderRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProductsInOrderRepository productsInOrderRepository;
    private final OrderMapper orderMapper;


    /**
     * Получение списка оформленных заказов
     * */
    public Flux<OrderDTO> findAllCompletedOrder() {
        List<OrderStatus> status= Arrays.asList(OrderStatus.IN_PROGRESS, OrderStatus.CLOSED);
        return orderRepository.findByStatusInAndDeletedIsFalse(status)
                .distinct(Order::getId)
                .flatMap(order ->
                        productsInOrderRepository.findAllByOrderId(order.getId())
                                .flatMap(item ->
                                        productRepository.findById(item.getProductId())
                                                .doOnNext(item::setProduct)
                                                .thenReturn(item)
                                )
                                .sort(Comparator.comparing(ProductsInOrder::getProductId))
                                .collectList()
                                .doOnNext(order::setProducts)
                                .thenReturn(order)
                )
                .sort(Comparator.comparing(Order::getId))
                .map(orderMapper::toDto);
    }

    /**
     * Оформление заказа
     * */
    public Mono<OrderDTO> addNewOrder() {
        return getOrCreateCartOrderWithCleanup()
                .flatMap(order -> {
                    order.setStatus(OrderStatus.IN_PROGRESS);
                    order.setUpdatedAt(LocalDateTime.now());
                    return orderRepository.save(order);
                })
                .map(orderMapper::toDto);
    }

    /**
     * Получение/создание заказа в статусе Create (корзина)
     * */
    public Mono<Order> getOrCreateCartOrderWithCleanup() {
        List<OrderStatus> status = Arrays.asList(OrderStatus.CREATE);

        return orderRepository.findFirstByStatusInAndDeletedIsFalseOrderByCreatedAtDesc(status)
                //удаляем лишниe заказы в статусе Create
                .flatMap(order -> {
                    return deactivateOtherCreateOrders(order)
                            .thenReturn(order);
                })
                //собираем список продуктов в корзине
                .flatMap(order -> {
                    return enrichOrderWithProducts(order)
                            .thenReturn(order);
                })
                // Заказ не найден - создаем новый
                .switchIfEmpty(createNewOrder());
    }

    /**
     * Деактивация старых заказов в статусе create
     * */
    public Mono<Void> deactivateOtherCreateOrders(Order excludedOrder) {
        return orderRepository.findByStatusInAndDeletedIsFalse(List.of(OrderStatus.CREATE))
                .filter(order -> !order.getId().equals(excludedOrder.getId()))
                .doOnNext(order -> {
                    order.setDeleted(true);
                    order.setUpdatedAt(LocalDateTime.now());
                })
                .collectList()
                .flatMap(ordersToDeactivate -> {
                    if (!ordersToDeactivate.isEmpty()) {
                        return orderRepository.saveAll(ordersToDeactivate).then();
                    }
                    return Mono.empty();
                });
    }

    private Mono<Order> enrichOrderWithProducts(Order order) {
        return productsInOrderRepository.findAllByOrderId(order.getId())
                .flatMap(this::enrichOrderItemWithProduct)
                .collectList()
                .doOnNext(order::setProducts)
                .thenReturn(order);
    }

    private Mono<ProductsInOrder> enrichOrderItemWithProduct(ProductsInOrder item) {
        return productRepository.findById(item.getProductId())
                .doOnNext(item::setProduct)
                .thenReturn(item);
    }

    /**
     * Создание нового заказа для заполнения
     * */
    public Mono<Order> createNewOrder() {
        Order order = new Order();
        order.setStatus(OrderStatus.CREATE);
        return orderRepository.save(order)
                .doOnSuccess(savedOrder -> log.info("Order created with ID: {}", savedOrder.getId()))
                .doOnError(error -> log.error("Failed to create order", error));
    }


    /**
     * Получение не оформленного заказа (из корзины)
     * */
    public Mono<OrderDTO> getOrderInCart() {
        return getOrCreateCartOrderWithCleanup()
                .map(orderMapper::toDto);
    }

    /**
     * Получение заказа по идентификатору
     * */
    public Mono<OrderDTO> findById(Integer id) {
        return orderRepository.findById(id)
                .switchIfEmpty(Mono.error(new BadRequestException("Incorrect order id")))
                .flatMap(order ->
                        productsInOrderRepository.findAllByOrderId(order.getId())
                                .flatMap(item ->
                                        productRepository.findById(item.getProductId())
                                                .doOnNext(item::setProduct)
                                                .thenReturn(item)
                                )
                                .collectList()
                                .doOnNext(order::setProducts)
                                .thenReturn(order)
                )
                .map(orderMapper::toDto);
    }

    /**
     * Добавление/обновление товара в корзину
     * */
    public Mono<Void> addProductInCart(Integer productId, Integer countProduct) {
        return getOrderInCart().flatMap(order ->
            productsInOrderRepository.findFirstByOrderIdAndProductId(order.getId(), productId)
                    .hasElement()
                    .flatMap(exist -> {
                        if (exist) {
                            return productsInOrderRepository.findFirstByOrderIdAndProductId(order.getId(), productId)
                                    .flatMap(existingProduct -> {
                                        existingProduct.setProductCount(existingProduct.getProductCount() + countProduct);
                                        return productsInOrderRepository.save(existingProduct);
                                    });
                        } else {
                            return setProductInOrder(order.getId(), productId, countProduct);
                        }
                    })
        ).then();
    }

    /**
     * Добавление товара в корзину
     * */
    public Mono<Void> setProductInOrder(Integer orderId, Integer productId, Integer countProduct) {
        ProductsInOrder productsInOrder = new ProductsInOrder();
        productsInOrder.setOrderId(orderId);
        productsInOrder.setProductId(productId);
        productsInOrder.setProductCount(countProduct);
        return productsInOrderRepository.save(productsInOrder).then();
    }

    /**
     * Удаление товара из корзины
     * */
    public Mono<Integer> deleteProductInOrder(Integer itemId) {
        return productsInOrderRepository.findById(itemId)
                .switchIfEmpty(Mono.error(new BadRequestException("There is no such Product in the cart")))
                .flatMap(item -> {
                    Integer productId = item.getProductId();
                    return productsInOrderRepository.deleteById(item.getId())
                            .thenReturn(productId);
                    });
    }

    /**
     * Изменение количества товара из корзины
     * */
    public Mono<Integer> editProductInOrder(Integer itemId, Integer quantity) {
        return productsInOrderRepository.findById(itemId)
                .switchIfEmpty(Mono.error(new BadRequestException("There is no such Product in the cart")))
                .flatMap(item -> {
                    // Валидация
                    if (quantity == null) {
                        return Mono.error(new BadRequestException("Количество не указано"));
                    }
                    if (item.getProductCount() == null) {
                        return Mono.error(new BadRequestException("окличетсво товара не указано"));
                    }
                    Integer productId = item.getProductId();
                    int countProduct = item.getProductCount() + quantity;
                    if (countProduct > 0) {
                        item.setProductCount(countProduct);
                       return productsInOrderRepository.save(item)
                                .thenReturn(productId);
                    } else {
                        return productsInOrderRepository.deleteById(item.getId())
                                .thenReturn(productId);
                    }
                });
    }


    public Mono<ProductsInOrder> findByOrderIdAndProductId(Integer orderId, Integer productId) {
        return productsInOrderRepository.findFirstByOrderIdAndProductId(orderId, productId).switchIfEmpty( Mono.empty());

    }

    /**
     * Изменение статуса заказа в CLOSED раз в 5 секунд (эмитация обработки)
     * */
    @Scheduled(fixedDelay = 1000L*5)
    void closeOrderInProgress() {
        orderRepository.findByStatusInAndDeletedIsFalse(List.of(OrderStatus.IN_PROGRESS))
                .collectList()
                .flatMap(orders -> {
                    if (!orders.isEmpty()) {
                        orders.forEach(order -> order.setStatus(OrderStatus.CLOSED));
                        return orderRepository.saveAll(orders).then();
                    }
                    return Mono.empty();
                })
                .subscribe(
                        null,
                        error -> log.error("Failed to close orders in progress", error),
                        () -> log.debug("Order closing job completed")
                );

    }


}
