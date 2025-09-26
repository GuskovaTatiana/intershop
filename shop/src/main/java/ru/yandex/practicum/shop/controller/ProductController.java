package ru.yandex.practicum.shop.controller;

import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.shop.model.User;
import ru.yandex.practicum.shop.model.dto.FilterProductDTO;
import ru.yandex.practicum.shop.model.dto.OrderDTO;
import ru.yandex.practicum.shop.service.OrderService;
import ru.yandex.practicum.shop.service.ProductService;
import ru.yandex.practicum.shop.service.UserService;


@Controller
@AllArgsConstructor
@RequestMapping("/product")
public class ProductController {

    private final ProductService productService;
    private final OrderService orderService;
    private  final UserService userService;

    /**
     * Получение списка продуктов
     * */
    @GetMapping
    public Mono<String> listProduct(@ModelAttribute FilterProductDTO productFilter,
                                    @RequestParam(value = "error", required = false) String error,
                                    Model model, Authentication authentication) {
        Mono<OrderDTO> orderMono = Mono.empty();
        if (authentication != null && authentication.isAuthenticated()) {
            orderMono = userService.findByLogin(authentication.getName()).flatMap(authUser ->
                    orderService.getOrderInCart(authUser.getId())
            );
        }

        return orderMono
                .flatMap(order -> productService.getProductsByFilter(order, productFilter))
                .switchIfEmpty(productService.getProductsByFilter(null, productFilter))
                .flatMap(products -> {
                    model.addAttribute("products", products.getContent());
                    model.addAttribute("paging", products);
                    model.addAttribute("filter", productFilter);
                    model.addAttribute("error", error);
                    return Mono.just("main");
                });
    }

    /**
     * Открытие страницы с описанием продукта
     * */
    @GetMapping("/{productId}")
    public Mono<String> getProductById(
            @PathVariable int productId,
            Model model, Authentication authentication) {
        Mono<OrderDTO> orderMono = Mono.empty();
        if (authentication != null && authentication.isAuthenticated()) {
            orderMono = userService.findByLogin(authentication.getName()).flatMap(authUser ->
                    orderService.getOrderInCart(authUser.getId())
            );
        }
        return orderMono
                .flatMap(order -> productService.getProductById(order.getId(), productId))
                .switchIfEmpty(productService.getProductById(null, productId))
                .doOnNext(product -> model.addAttribute("product", product)) // Передаём готовый объект в модель
                .map(product -> "product");
    }
}
