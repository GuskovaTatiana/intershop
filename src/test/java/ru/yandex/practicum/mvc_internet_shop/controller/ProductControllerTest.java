package ru.yandex.practicum.mvc_internet_shop.controller;


import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.yandex.practicum.mvc_internet_shop.service.ProductService;
import ru.yandex.practicum.mvc_internet_shop.utils.TestDataUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

@WebMvcTest(ProductController.class)
public class ProductControllerTest {

    @MockitoBean
    private ProductService productService;
    @Autowired
    private MockMvc mockMvc;
    private TestDataUtils testData = new TestDataUtils();

    //Получение списка продуктов
    @Test
    void getListOfProducts_shouldReturnHtmlWithProducts() throws Exception  {
        Mockito.when(productService.getProductsByFilter(any())).thenReturn(testData.getListProduct());
        mockMvc.perform(get("/product")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(view().name("main"))
                .andExpect(model().attributeExists("products"))
                .andExpect(xpath("//div[@id='products-container']/div").nodeCount(3))
                .andExpect(MockMvcResultMatchers.xpath("//div[@id='products-container']/div[1]/div/a/h3").string("Товар 1"));
    }

    //Открытие страницы с описанием продукта
    @Test
    void getProductById_shouldReturnHtmlWithProductInfo() throws Exception  {
        Mockito.when(productService.getProductById(any())).thenReturn(testData.getProduct(3, "Товар 3", "/images/image3.png", "Описание 3", 25));
        mockMvc.perform(get("/product/{productId}", 3))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(view().name("product"))
                .andExpect(model().attributeExists("product"))
                .andExpect(xpath("//div[@id='item']").nodeCount(1))
                .andExpect(xpath("//div[@id='item']/h3").string("Товар 3"));
    }
}
