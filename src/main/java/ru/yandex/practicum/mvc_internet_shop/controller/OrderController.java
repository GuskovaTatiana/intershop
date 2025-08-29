package ru.yandex.practicum.mvc_internet_shop.controller;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.yandex.practicum.mvc_internet_shop.model.dto.OrderDTO;
import ru.yandex.practicum.mvc_internet_shop.service.OrderService;

import java.util.List;

@Controller
@AllArgsConstructor
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;

    /**
     * Оформление заказа
     * */
    @PostMapping
    public String createOrder(
            RedirectAttributes redirectAttributes) {
        OrderDTO order = orderService.addNewOrder();
        redirectAttributes.addAttribute("orderId", order.getId());
        return "redirect:/orders/{orderId}"; // Открывает страницу с заказом
    }

    /**
     * Получение списка заказов
     * */
    @GetMapping
    public String getOrders(
            Model model) {
        List<OrderDTO> dto = orderService.findAllCompletedOrder();
        model.addAttribute("orders", dto);
        return "orders"; // Открывает страницу с заказом
    }

    /**
     * Получение заказа по идентификатору
     * */
    @GetMapping("/{orderId}")
    public String getOrderById(
            @PathVariable int orderId,
            Model model) {
        OrderDTO dto = orderService.findById(orderId);
        model.addAttribute("order", dto);
        return "order"; // Открывает страницу с заказом
    }
}
