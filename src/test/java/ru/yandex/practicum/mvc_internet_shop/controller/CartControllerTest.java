package ru.yandex.practicum.mvc_internet_shop.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.mvc_internet_shop.model.enums.OrderStatus;
import ru.yandex.practicum.mvc_internet_shop.service.OrderService;
import ru.yandex.practicum.mvc_internet_shop.utils.TestDataUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

@WebMvcTest(CartController.class)
public class CartControllerTest {

    @MockitoBean
    private OrderService orderService;
    @Autowired
    private MockMvc mockMvc;

    private TestDataUtils testData = new TestDataUtils();

    //Добавление продукта в корзину с редиректом на страницу Каталог
    @Test
    void addProductToOrder_shouldRedirectToHtmlWithListProduct() throws Exception  {
        Mockito.doNothing().when(orderService).addProductInCart(any(), any());
        mockMvc.perform(post("/cart/{productId}/addItem" , 1))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/product"));
    }

    //Добавление продукта в корзину с редиректом на страницу Товар
    @Test
    void addProductToOrder_shouldRedirectToHtmlWithProductId() throws Exception  {
        Mockito.doNothing().when(orderService).addProductInCart(any(), any());
        mockMvc.perform(post("/cart/{productId}/addItem" , 1)
                        .param("redirectTo", "product/{productId}"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/product/1"));
    }

    //Открытие страницы Корзина
    @Test
    void getCart_shouldReturnHtmlWithListProductInCart() throws Exception  {
        Mockito.when(orderService.getOrderInCart()).thenReturn(testData.getOrder(1, OrderStatus.CREATE, testData.getListProduct().getContent()));
        mockMvc.perform(get("/cart" ))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(view().name("cart"))
                .andExpect(model().attributeExists("products"))
                .andExpect(model().attributeExists("total"))
                .andExpect(xpath("//div[@id='products-container']/div").nodeCount(3));
    }
}
