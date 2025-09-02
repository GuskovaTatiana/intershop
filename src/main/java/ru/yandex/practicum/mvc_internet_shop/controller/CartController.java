package ru.yandex.practicum.mvc_internet_shop.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mvc_internet_shop.model.dto.ProductDTO;
import ru.yandex.practicum.mvc_internet_shop.service.OrderService;

import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@Slf4j
@AllArgsConstructor
@RequestMapping("/cart")
public class CartController {
    private final OrderService orderService;

    /**
     * Открытие страницы Корзина
     * */
    @GetMapping
    public Mono<String> getCart(
            Model model) {
        // получаем список продуктов в корзине
        return orderService.getOrderInCart()
                .flatMap(order -> {
                    List<ProductDTO> sortedProducts = order.getProducts().stream()
                            .sorted(Comparator.comparing(ProductDTO::getId))
                            .collect(Collectors.toList());

                    return Mono.fromCallable(() -> {
                        model.addAttribute("products", sortedProducts);
                        model.addAttribute("total", order.getTotalPrice());
                        // Открывает страницу со списком товаров
                        return "cart";
                    });
                });
    }

    /**
     * Увеличение/ уменьшение количества продуктов
     * */
    @PostMapping("/item/{id}/edit")
    public Mono<ResponseEntity<Void>> editCountProductFromOrder(
            @PathVariable int id,
            @RequestParam Integer quantity,
            @RequestParam(defaultValue = "product") String redirectTo
          ) {
        return orderService.editProductInOrder(id, quantity)
                .map(productId -> {
                    String redirectUrl = redirectTo.replace("{productId}", String.valueOf(productId));
                    return ResponseEntity.status(HttpStatus.FOUND)
                            .location(URI.create("/" + redirectUrl))
                            .build();
                });
    }

    /**
     * Удаление продукта из корзины
     * */
    @PostMapping("/item/{id}/delete")
    public Mono<ResponseEntity<Void>> deleteProductFromOrder(
            @PathVariable int id,
            @RequestParam(defaultValue = "product") String redirectTo) {
        return orderService.deleteProductInOrder(id)
                .map(productId -> {
                    String redirectUrl = redirectTo.replace("{productId}", String.valueOf(productId));
                    return ResponseEntity.status(HttpStatus.FOUND)
                            .location(URI.create("/" + redirectUrl))
                            .build();
                });
    }

    /**
     * Добавление продукта в корзину
     * */
    @PostMapping("/{productId}/addItem")
    public Mono<ResponseEntity<Void>> addProductToOrder(
            @PathVariable int productId,
            @RequestParam(defaultValue = "product") String redirectTo
    ) {
        return orderService.addProductInCart(productId, 1)
                .thenReturn(ResponseEntity.status(HttpStatus.FOUND)
                        .location(URI.create("/" + redirectTo.replace("{productId}", String.valueOf(productId))))
                        .build());
    }

}
