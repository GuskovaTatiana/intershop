package ru.yandex.practicum.shop.controller;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.shop.service.OrderService;

@Controller
@AllArgsConstructor
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;

    /**
     * Оформление заказа
     * */
    @PostMapping
    public Mono<String> createOrder(@RequestParam String amount) {
        return orderService.addNewOrder(Integer.valueOf(amount))
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
