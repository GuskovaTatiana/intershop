package ru.yandex.practicum.mvc_internet_shop.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mvc_internet_shop.service.OrderService;

import java.net.URI;

@Controller
@AllArgsConstructor
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;

    /**
     * Оформление заказа
     * */
    @PostMapping
    public Mono<String> createOrder() {
        return orderService.addNewOrder()
                .map(order -> {
                    String redirectUrl = "orders/" + order.getId();
                                return "redirect:/" + redirectUrl;
                });
    }

    /**
     * Получение списка заказов
     * */
    @GetMapping
    public Mono<String> getOrders(
            Model model) {
        return orderService.findAllCompletedOrder()
                .collectList()
                .doOnNext(orders -> {
                    model.addAttribute("orders", orders);
                    model.addAttribute("ordersCount", orders.size());
                })
                .then(Mono.just("orders"));

    }

    /**
     * Получение заказа по идентификатору
     * */
    @GetMapping("/{orderId}")
    public Mono<String> getOrderById(
            @PathVariable int orderId,
            Model model) {
        return orderService.findById(orderId)
                .doOnNext(order -> model.addAttribute("order", order)) // Передаём готовый объект в модель
                .map(order -> "order"); // Открывает страницу с заказом
    }
}
