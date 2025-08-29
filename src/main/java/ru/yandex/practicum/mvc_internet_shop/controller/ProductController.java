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
import ru.yandex.practicum.mvc_internet_shop.model.Order;
import ru.yandex.practicum.mvc_internet_shop.model.dto.FilterProductDTO;
import ru.yandex.practicum.mvc_internet_shop.model.dto.OrderDTO;
import ru.yandex.practicum.mvc_internet_shop.model.dto.ProductDTO;
import ru.yandex.practicum.mvc_internet_shop.service.OrderService;
import ru.yandex.practicum.mvc_internet_shop.service.ProductService;

import java.util.List;

@Controller
@AllArgsConstructor
@RequestMapping("/product")
public class ProductController {

    private final ProductService productService;
    private final OrderService orderService;
    private static FilterProductDTO filter = new FilterProductDTO(0, 10, "", "title asc");

    /**
     * Получение списка продуктов
     * */
    @GetMapping
    public String listProduct( @ModelAttribute FilterProductDTO productFilter,
            Model model) {
        filter.copy(productFilter);

        Page<ProductDTO> dto = productService.getProductsByFilter(filter);

        model.addAttribute("products", dto.getContent());
        model.addAttribute("paging", dto);
        model.addAttribute("filter", filter);
        return "main"; // Открывает страницу со списком товаров
    }

    /**
     * Открытие страницы с описанием продукта
     * */
    @GetMapping("/{productId}")
    public String getProductById(
            @PathVariable int productId,
            Model model) {
        ProductDTO dto = productService.getProductById(productId);
        model.addAttribute("product", dto);
        return "product"; // Открывает страницу товара
    }









}
