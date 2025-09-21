package ru.yandex.practicum.shop.controller;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import reactor.core.publisher.Mono;
import ru.yandex.practicum.shop.model.dto.FilterProductDTO;
import ru.yandex.practicum.shop.service.OrderService;
import ru.yandex.practicum.shop.service.ProductService;


@Controller
@AllArgsConstructor
@RequestMapping("/product")
public class ProductController {

    private final ProductService productService;
    private final OrderService orderService;

    /**
     * Получение списка продуктов
     * */
    @GetMapping
    public Mono<String> listProduct(@ModelAttribute FilterProductDTO productFilter,
                                    Model model) {

        return orderService.getOrderInCart()
                .flatMap(order -> productService.getProductsByFilter(order, productFilter))
                .flatMap(products -> {
                    model.addAttribute("products", products.getContent());
                    model.addAttribute("paging", products);
                    model.addAttribute("filter", productFilter);
                    return Mono.just("main");
                });
    }

    /**
     * Открытие страницы с описанием продукта
     * */
    @GetMapping("/{productId}")
    public Mono<String> getProductById(
            @PathVariable int productId,
            Model model) {
        return orderService.getOrderInCart()
                        .flatMap(order -> productService.getProductById(order.getId(), productId))
                .doOnNext(product -> model.addAttribute("product", product)) // Передаём готовый объект в модель
                .map(product -> "product");
    }
}
