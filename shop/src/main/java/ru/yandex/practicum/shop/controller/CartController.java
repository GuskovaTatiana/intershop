package ru.yandex.practicum.shop.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.shop.model.BalanceReplenishmentRequest;
import ru.yandex.practicum.shop.model.dto.OrderDTO;
import ru.yandex.practicum.shop.model.dto.ProductDTO;
import ru.yandex.practicum.shop.service.impl.OrderServiceImpl;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@Slf4j
@AllArgsConstructor
@RequestMapping("/cart")
public class CartController {
    private final OrderServiceImpl orderService;

    /**
     * Открытие страницы Корзина
     * */
    @GetMapping
    public Mono<String> getCart(
            Model model) {
        // получаем список продуктов в корзине
        return orderService.getOrderInCart()
                .zipWith(orderService.getBalance()
                        .onErrorReturn(-1))
                .flatMap(tuple -> {
                    OrderDTO order = tuple.getT1();
                    Integer balance = tuple.getT2();

                    String errorMessage = "";
                    boolean psUnavailable = false;
                    if (balance == -1) {
                        psUnavailable = true;
                        errorMessage = "Платежный сервис временно недоступен";
                        balance = 0;
                    } else if (balance < order.getTotalPrice()) {
                        errorMessage = "Недостаточно средств на балансе";
                    }
                    List<ProductDTO> sortedProducts = order.getProducts().stream()
                            .sorted(Comparator.comparing(ProductDTO::getId))
                            .collect(Collectors.toList());
                    Integer finalBalance = balance;
                    String finalMessageError = errorMessage;
                    boolean finalPsUnavailable = psUnavailable;
                    return Mono.fromCallable(() -> {
                        model.addAttribute("products", sortedProducts);
                        model.addAttribute("total", order.getTotalPrice());
                        model.addAttribute("balance", finalBalance);
                        model.addAttribute("errorMessage", finalMessageError);
                        model.addAttribute("paymentServiceUnavailable",finalPsUnavailable);

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
