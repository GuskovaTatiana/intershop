package ru.yandex.practicum.shop.controller;

import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.shop.service.OrderService;
import ru.yandex.practicum.shop.service.UserService;

@Controller
@AllArgsConstructor
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;
    private final UserService userService;

    /**
     * Оформление заказа
     * */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Mono<String> createOrder(@RequestParam String amount, Authentication authUser) {
        // получаем список продуктов в корзине
        return userService.findByLogin(authUser.getName())
                .flatMap(user -> orderService.addNewOrder(user.getId(), Integer.valueOf(amount)))
                .map(order -> {
                    String redirectUrl = "orders/" + order.getId();
                                return "redirect:/" + redirectUrl;
                });
    }

    /**
     * Получение списка заказов
     * */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Mono<String> getOrders(Authentication authUser,
            Model model) {

        return userService.findByLogin(authUser.getName())
                .flatMap(user -> orderService.findAllCompletedOrder(user.getId())
                .collectList())
                .doOnNext(orders -> {
                    model.addAttribute("orders", orders);
                    model.addAttribute("ordersCount", orders.size());
                })
                .then(Mono.just("orders"));

    }

    /**
     * Получение заказа по идентификатору
     * */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{orderId}")
    public Mono<String> getOrderById(
            @PathVariable int orderId,
            Authentication authUser,
            Model model) {
        return userService.findByLogin(authUser.getName())
                .flatMap(user -> orderService.findById(orderId, user.getId()))
                .doOnNext(order -> model.addAttribute("order", order)) // Передаём готовый объект в модель
                .map(order -> "order"); // Открывает страницу с заказом
    }
}
