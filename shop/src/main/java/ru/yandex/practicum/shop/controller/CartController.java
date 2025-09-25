package ru.yandex.practicum.shop.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.shop.model.BalanceReplenishmentRequest;
import ru.yandex.practicum.shop.model.dto.FilterProductDTO;
import ru.yandex.practicum.shop.model.dto.OrderDTO;
import ru.yandex.practicum.shop.model.dto.ProductDTO;
import ru.yandex.practicum.shop.service.OrderService;
import ru.yandex.practicum.shop.service.UserService;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@Slf4j
@AllArgsConstructor
@RequestMapping("/cart")
public class CartController {
    private final OrderService orderService;
    private final UserService userService;

    /**
     * Открытие страницы Корзина
     * */
    @GetMapping
    public Mono<String> getCart(
            Model model, Authentication authUser) {
        // получаем список продуктов в корзине
        if (authUser == null || !authUser.isAuthenticated()) {
            return Mono.just("redirect:/product");
        }
        return userService.findByLogin(authUser.getName()).flatMap(user -> {
                    return orderService.getOrderInCart(user.getId())
                            .zipWith(orderService.getBalance(user.getId())
                                    .onErrorReturn(-1));
        })
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
            @ModelAttribute FilterProductDTO productFilter,
            @RequestParam(defaultValue = "product") String redirectTo,
            Authentication authUser
          ) {
        if (authUser == null || !authUser.isAuthenticated()) {
            return Mono.just("redirect:/product");
        }
        //todo сделать проверку принадлежности продукта в корзине авторизованному пользователю
        return orderService.editProductInOrder(id, quantity, authUser)
                .map(productId -> {
                    String redirectUrl = redirectTo.replace("{productId}", String.valueOf(productId));
                    if (productFilter != null && redirectUrl.equals("product")){
                        redirectUrl = buildRedirectUrl(redirectUrl, productFilter);
                    }
                    return "redirect:/" + redirectUrl;
                });
    }


    /**
     * Удаление продукта из корзины
     * */
    @PostMapping("/item/{id}/delete")
    public Mono<String> deleteProductFromOrder(
            @PathVariable int id,
            @ModelAttribute FilterProductDTO productFilter,
            @RequestParam(defaultValue = "product") String redirectTo,
            Authentication authUser) {
        if (authUser == null || !authUser.isAuthenticated()) {
            return Mono.just("redirect:/product");
        }
        return orderService.deleteProductInOrder(id, authUser)
                .map(productId -> {
                    String redirectUrl = redirectTo.replace("{productId}", String.valueOf(productId));
                    if (productFilter != null && redirectUrl.equals("product")){
                        redirectUrl = buildRedirectUrl(redirectUrl, productFilter);
                    }
                    return "redirect:/" + redirectUrl;
                });
    }

        /**
         * Добавление продукта в корзину
         * */
        @PostMapping("/{productId}/addItem")
        public Mono<String> addProductToOrder(
                @PathVariable int productId,
                @ModelAttribute FilterProductDTO productFilter,
                @RequestParam(defaultValue = "product") String redirectTo,
                Authentication authUser
        ) {
            if (authUser == null || !authUser.isAuthenticated()) {
                return Mono.just("redirect:/product");
            }
            return userService.findByLogin(authUser.getName())
                    .flatMap(user -> orderService.addProductInCart(user.getId(), productId, 1)
                    .then(Mono.fromCallable(() -> {
                        String redirectUrl = redirectTo.replace("{productId}", String.valueOf(productId));

                        // Добавляем параметры фильтра, если они есть
                        if (productFilter != null && redirectTo.equals("product")) {
                            redirectUrl = buildRedirectUrl(redirectUrl, productFilter);
                        }

                        return "redirect:/" + redirectUrl;
                    })));
        }

    @PostMapping("/balance/replenishment")
    public Mono<String> replenishmentBalance(@ModelAttribute BalanceReplenishmentRequest balance,
                                             Authentication authUser) {
        return userService.findByLogin(authUser.getName())
                .flatMap(user -> orderService.setBalance(user.getId(), balance.getAmount()))
                .thenReturn("redirect:/cart");
    }


//
    private String buildRedirectUrl(String baseUrl, FilterProductDTO productFilter) {
        if (productFilter == null) {
            return baseUrl;
        }
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath(baseUrl);

        if (productFilter.getPage() != null) {
            builder.queryParam("page", productFilter.getPage());
        }
        if (productFilter.getSize() != null) {
            builder.queryParam("size", productFilter.getSize());
        }
        if (productFilter.getSearch() != null && !productFilter.getSearch().isEmpty()) {
            builder.queryParam("search", productFilter.getSearch());
        }
        if (productFilter.getSort() != null && !productFilter.getSort().isEmpty()) {
            builder.queryParam("sort", productFilter.getSort());
        }

        return builder.toUriString();
    }

}
