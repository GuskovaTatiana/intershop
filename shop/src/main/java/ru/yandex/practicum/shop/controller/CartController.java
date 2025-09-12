package ru.yandex.practicum.shop.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.model.BalanceResponse;
import ru.yandex.practicum.shop.model.BalanceReplenishmentRequest;
import ru.yandex.practicum.shop.model.Order;
import ru.yandex.practicum.shop.model.dto.FilterProductDTO;
import ru.yandex.practicum.shop.model.dto.OrderDTO;
import ru.yandex.practicum.shop.model.dto.ProductDTO;
import ru.yandex.practicum.shop.service.OrderService;

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
                .zipWith(orderService.getBalance())
                .flatMap(tuple -> {
                    OrderDTO order = tuple.getT1();
                    Integer balance = tuple.getT2();
                    List<ProductDTO> sortedProducts = order.getProducts().stream()
                            .sorted(Comparator.comparing(ProductDTO::getId))
                            .collect(Collectors.toList());

                    return Mono.fromCallable(() -> {
                        model.addAttribute("products", sortedProducts);
                        model.addAttribute("total", order.getTotalPrice());
                        model.addAttribute("balance", balance);
                        // Открывает страницу со списком товаров
                        return "cart";
                    });
                });
    }

    /**
     * Увеличение/ уменьшение количества продуктов
     * */
    @PostMapping("/item/{id}/edit")
    public Mono<String> editCountProductFromOrder(
            @PathVariable int id,
            @RequestParam Integer quantity,
            @RequestParam(defaultValue = "product") String redirectTo
          ) {
        return orderService.editProductInOrder(id, quantity)
                .map(productId -> {
                    String redirectUrl = redirectTo.replace("{productId}", String.valueOf(productId));
                    return "redirect:/" + redirectUrl;
                });
    }

    /**
     * Удаление продукта из корзины
     * */
    @PostMapping("/item/{id}/delete")
    public Mono<String> deleteProductFromOrder(
            @PathVariable int id,
            @RequestParam(defaultValue = "product") String redirectTo) {
        return orderService.deleteProductInOrder(id)
                .map(productId -> {
                    String redirectUrl = redirectTo.replace("{productId}", String.valueOf(productId));
                    return "redirect:/" + redirectUrl;
                });
    }

    /**
     * Добавление продукта в корзину
     * */
    @PostMapping("/{productId}/addItem")
    public Mono<String> addProductToOrder(
            @PathVariable int productId,
            @RequestParam(defaultValue = "product") String redirectTo
    ) {
        return orderService.addProductInCart(productId, 1)
                .thenReturn("redirect:/" + redirectTo.replace("{productId}", String.valueOf(productId)));
    }

    @PostMapping("/balance/replenishment")
    public Mono<String> replenishmentBalance(@ModelAttribute BalanceReplenishmentRequest balance) {
        return orderService.setBalance(balance.getAmount())
                .thenReturn("redirect:/cart");
    }

}
