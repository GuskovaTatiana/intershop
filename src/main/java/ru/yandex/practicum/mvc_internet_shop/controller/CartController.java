package ru.yandex.practicum.mvc_internet_shop.controller;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.yandex.practicum.mvc_internet_shop.model.dto.OrderDTO;
import ru.yandex.practicum.mvc_internet_shop.model.dto.ProductDTO;
import ru.yandex.practicum.mvc_internet_shop.service.OrderService;
import ru.yandex.practicum.mvc_internet_shop.service.ProductService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Controller
@AllArgsConstructor
@RequestMapping("/cart")
public class CartController {
    private final OrderService orderService;

    /**
     * Открытие страницы Корзина
     * */
    @GetMapping
    public String getCart(
            Model model) {
        // получаем/создаем заказ в статусе Create
        OrderDTO order = orderService.getOrderInCart();
        List<ProductDTO> dto = new ArrayList<>(order.getProducts()); // Создаем копию списка, чтобы не изменить исходный
        dto.sort(Comparator.comparing(ProductDTO::getId));
        model.addAttribute("products",dto);
         model.addAttribute("total" ,order.getTotalPrice());
        model.addAttribute("orderId", order.getId());
        return "cart"; // Открывает страницу со списком товаров
    }

    /**
     * Увеличение/ уменьшение количества продуктов
     * */
    @PostMapping("/item/{id}/edit")
    public String editCountProductFromOrder(
            @PathVariable int id,
            @RequestParam Integer quantity,
            @RequestParam(defaultValue = "product") String redirectTo,
            RedirectAttributes redirectAttributes) {
        int productId = orderService.editProductInOrder(id, quantity);
        redirectAttributes.addAttribute("productId", productId);
        return "redirect:/" + redirectTo; // Открывает страницу со списком товаров
    }

    /**
     * Удаление продукта из корзины
     * */
    @PostMapping("/item/{id}/delete")
    public String deleteProductFromOrder(
            @PathVariable int id,
            @RequestParam(defaultValue = "product") String redirectTo,
            RedirectAttributes redirectAttributes) {
        int productId = orderService.deleteProductInOrder(id);
        redirectAttributes.addAttribute("productId", productId);
        return "redirect:/" + redirectTo; // Открывает страницу со списком товаров
    }

    /**
     * Добавление продукта в корзину
     * */
    @PostMapping("/{productId}/addItem")
    public String addProductToOrder(
            @PathVariable int productId,
            @RequestParam(defaultValue = "product") String redirectTo,
            Model model) {
        orderService.addProductInCart(productId, 1);
        return "redirect:/" + redirectTo; // Открывает страницу со списком товаров
    }
}
