package ru.yandex.practicum.mvc_internet_shop.service;

import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.mvc_internet_shop.mapper.OrderMapper;
import ru.yandex.practicum.mvc_internet_shop.model.Order;
import ru.yandex.practicum.mvc_internet_shop.model.ProductsInOrder;
import ru.yandex.practicum.mvc_internet_shop.model.dto.OrderDTO;
import ru.yandex.practicum.mvc_internet_shop.model.enums.OrderStatus;
import ru.yandex.practicum.mvc_internet_shop.model.exception.BadRequestException;
import ru.yandex.practicum.mvc_internet_shop.repository.OrderRepository;
import ru.yandex.practicum.mvc_internet_shop.repository.ProductsInOrderRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductsInOrderRepository productsInOrderRepository;
    private final OrderMapper orderMapper;


    /**
     * Получение списка оформленных заказов
     * */
    public List<OrderDTO> findAllCompletedOrder() {
        List<OrderStatus> status= Arrays.asList(OrderStatus.IN_PROGRESS, OrderStatus.CLOSED);
        List<Order> orders = orderRepository.findByStatusInAndDeletedIsFalseOrderByCreatedAtDesc(status);

        return orderMapper.toDto(orders);
    }

    /**
     * Создание нового заказа для заполнения
     * */
    public Order createNewOrder() {
        Order order = new Order();
        order.setStatus(OrderStatus.CREATE);
        Order newOrder = orderRepository.saveAndFlush(order);
        return newOrder;
    }

    /**
     * Оформление заказа
     * */
    public OrderDTO addNewOrder() {
        Order order = findOrCreateOrderInCart();
        order.setStatus(OrderStatus.IN_PROGRESS);
        order.setUpdatedAt(LocalDateTime.now());
        Order newOrder = orderRepository.saveAndFlush(order);
        return orderMapper.toDto(newOrder);
    }

    /**
     * Получение/создание заказа в статусе Create (корзина)
     * */
    @Transactional(readOnly = true)
    public Order findOrCreateOrderInCart() {
        List<OrderStatus> status= Arrays.asList(OrderStatus.CREATE);
        List<Order> orders = orderRepository.findByStatusInAndDeletedIsFalseOrderByCreatedAtDesc(status);
        // если такого заказа нет, то возвращаем null
        if (orders == null || orders.isEmpty()) {
            return createNewOrder();
        }
        // забираем последний созданный
        Order order = orders.getFirst();
        // удаляем все пустые заказы в статусе CREATE
        if (orders.size() > 1) {
            orders.removeIf(it -> it.getId().equals(order.getId()));
            orders.forEach(it -> it.setDeleted(true));
            orderRepository.saveAll(orders);
        }
       return order;
    }

    /**
     * Получение не оформленного заказа (из корзины)
     * */
    public OrderDTO getOrderInCart() {
        Order order = findOrCreateOrderInCart();
        return orderMapper.toDto(order);
    }


    /**
     * Получение заказа по идентификатору
     * */
    @Transactional(readOnly = true)
    public OrderDTO findById(Integer id) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new BadRequestException("Incorrect order id"));;

        return orderMapper.toDto(order);
    }


    /**
     * Добавление/обновление товара в корзину
     * */
    public void addProductInCart(Integer productId, Integer countProduct) {
        Order order = findOrCreateOrderInCart();

        //проверяем наличие товара в заказе, если нет, то заливаем новую запись, если есть обновляем имеющуюся
        if (order.getProducts() != null) {
            List<ProductsInOrder> products = order.getProducts().stream().filter(it -> it.getProductId().equals(productId)).collect(Collectors.toList());
            if (!products.isEmpty()) {
                products.forEach(it -> it.setProductCount(it.getProductCount() + countProduct));
                productsInOrderRepository.saveAll(products);
            } else {
                setProductInOrder(order.getId(), productId, countProduct);
            }
        } else {
            setProductInOrder(order.getId(), productId, countProduct);
        }
    }

    /**
     * Добавление товара в корзину
     * */
    public ProductsInOrder setProductInOrder(Integer orderId, Integer productId, Integer countProduct) {
        ProductsInOrder productsInOrder = new ProductsInOrder();
        productsInOrder.setOrderId(orderId);
        productsInOrder.setProductId(productId);
        productsInOrder.setProductCount(countProduct);
        return productsInOrderRepository.save(productsInOrder);
    }

    /**
     * Удаление товара из корзины
     * */
    public Integer deleteProductInOrder(Integer itemId) {
        ProductsInOrder item = productsInOrderRepository.findById(itemId).orElseThrow(() -> new BadRequestException("There is no such Product in the cart"));
        productsInOrderRepository.delete(item);

        return item.getProductId();
    }


    /**
     * Изменение количества товара из корзины
     * */
    public Integer editProductInOrder(Integer itemId, Integer quantity) {
        ProductsInOrder item = productsInOrderRepository.findById(itemId).orElseThrow(() -> new BadRequestException("There is no such Product in the cart"));
        Integer countProduct = item.getProductCount() + quantity;
        if (countProduct > 0) {
            item.setProductCount(countProduct);
            productsInOrderRepository.save(item);
        } else {
            productsInOrderRepository.delete(item);
        }
        return item.getProductId();
    }

    /**
     * Изменение статуса заказа в CLOSED раз в 5 секунд (эмитация обработки)
     * */
    @Scheduled(fixedDelay = 1000L*5)
    void closeOrderInProgress() {
        List<OrderStatus> status= Arrays.asList(OrderStatus.IN_PROGRESS);
        List<Order> orders = orderRepository.findByStatusInAndDeletedIsFalseOrderByCreatedAtDesc(status);
        if (!orders.isEmpty()) {
            orders.forEach(it -> it.setStatus(OrderStatus.CLOSED));
            orderRepository.saveAll(orders);
        }

    }


}
