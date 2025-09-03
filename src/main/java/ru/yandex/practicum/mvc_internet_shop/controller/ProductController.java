package ru.yandex.practicum.mvc_internet_shop.controller;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import reactor.core.publisher.Mono;
import ru.yandex.practicum.mvc_internet_shop.model.dto.FilterProductDTO;
import ru.yandex.practicum.mvc_internet_shop.service.ProductService;


@Controller
@AllArgsConstructor
@RequestMapping("/product")
public class ProductController {

    private final ProductService productService;
    private static FilterProductDTO filter = new FilterProductDTO(0, 10, "", "title asc");

    /**
     * Получение списка продуктов
     * */
    @GetMapping
    public Mono<String> listProduct(@ModelAttribute FilterProductDTO productFilter,
                                    Model model) {
        filter.copy(productFilter);
        return productService.getProductsByFilter(filter)
                .doOnNext(products -> {
                    model.addAttribute("products", products.getContent());
                    model.addAttribute("paging", products);
                    model.addAttribute("filter", filter);
                }).map(product -> "main"); // Открывает страницу со списком товаров
    }

    /**
     * Открытие страницы с описанием продукта
     * */
    @GetMapping("/{productId}")
    public Mono<String> getProductById(
            @PathVariable int productId,
            Model model) {
        return productService.getProductById(productId)
                .doOnNext(product -> model.addAttribute("product", product)) // Передаём готовый объект в модель
                .map(product -> "product");
    }
}
