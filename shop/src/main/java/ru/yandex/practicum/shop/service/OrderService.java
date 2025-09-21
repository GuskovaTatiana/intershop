package ru.yandex.practicum.shop.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.model.AmountRequest;
import ru.yandex.practicum.model.BalanceResponse;
import ru.yandex.practicum.model.PaymentBody;
import ru.yandex.practicum.shop.mapper.OrderMapper;
import ru.yandex.practicum.shop.model.Order;
import ru.yandex.practicum.shop.model.Product;
import ru.yandex.practicum.shop.model.ProductsInOrder;
import ru.yandex.practicum.shop.model.dto.OrderDTO;
import ru.yandex.practicum.shop.model.enums.OrderStatus;
import ru.yandex.practicum.shop.model.exception.BadRequestException;
import ru.yandex.practicum.shop.repository.OrderRepository;
import ru.yandex.practicum.shop.repository.ProductsInOrderRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductsInOrderRepository productsInOrderRepository;

    private final PaymentServiceClient paymentService;
    private final ProductService productService;
    private final OrderMapper orderMapper;



    /**
     * Получение списка оформленных заказов
     * */
    public Flux<OrderDTO> findAllCompletedOrder() {
        List<OrderStatus> status= Arrays.asList(OrderStatus.IN_PROGRESS, OrderStatus.CLOSED);
        return orderRepository.findByStatusInAndDeletedIsFalse(status)
                .distinct(Order::getId)
                .flatMap(this::findProductsInOrder)
                .sort(Comparator.comparing(Order::getId))
                .map(orderMapper::toDto);
    }

    /**
     * Оформление заказа
     * */
    public Mono<OrderDTO> addNewOrder(Integer amount) {
        PaymentBody payment =  new PaymentBody();
        payment.setAmount(amount);
        return paymentService.processPayment(payment)
                .flatMap(result -> {
                    if (result.getSuccess() == true) {
                        return getOrCreateCartOrderWithCleanup()
                                .flatMap(order -> {
                                    order.setStatus(OrderStatus.IN_PROGRESS);
                                    order.setUpdatedAt(LocalDateTime.now());
                                    return orderRepository.save(order)
                                            .map(savedOrder -> orderMapper.toDto(savedOrder));
                                });
                    } else {
                        return Mono.error(new BadRequestException(result.getMessage()));
                    }
                });
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
                .flatMap(this::findProductsInOrder)
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
                .flatMap(this::findProductsInOrder)
                .map(orderMapper::toDto);
    }

    /**
     * Получение списка товаров в заказе
     * */
    public Mono<Order> findProductsInOrder(Order order) {
        return productsInOrderRepository.findAllByOrderId(order.getId())
                .collectList()
                .flatMap(this::setProductInItem)
                .doOnNext(order::setProducts)
                .thenReturn(order);
    }


    /**
     * Фильтрация по идентификаторам и сортировка списка продуктов
     */
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
                        return Mono.error(new BadRequestException("Количество товара не указано"));
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



    /**
     * Изменение статуса заказа в CLOSED раз в 5 секунд (эмитация обработки)
     * */
    @Scheduled(fixedDelay = 1000L*5)
    void closeOrderInProgress() {
        orderRepository.findByStatusInAndDeletedIsFalse(List.of(OrderStatus.IN_PROGRESS))
                .collectList()
                .flatMap(orders -> {
                    if (!orders.isEmpty()) {
                        orders.stream().filter(it -> it.getUpdatedAt().isBefore(LocalDateTime.now().minusMinutes(5)));
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


    /**
     * Получение баланса пользователя
     * */
    public Mono<Integer> getBalance() {
        return paymentService.getBalance()
                .map(BalanceResponse::getBalance)
                .onErrorResume(e -> {
                    log.warn("Failed to get balance {}", e.getMessage());
                    return Mono.just(-1);
                });
    }

    /**
     * Пополнение баланса пользователя
     * */
    public Mono<Boolean> setBalance(Integer amount) {
        AmountRequest amountRequest = new AmountRequest();
        amountRequest.setDepositAmount(amount);
        return paymentService.setBalance(amountRequest)
                .thenReturn(true)
                .onErrorResume(e -> {
                    log.warn("Failed to set balance {}", e.getMessage());
                    return Mono.just(false);
                });
    }


}
