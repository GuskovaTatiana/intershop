package ru.yandex.practicum.mvc_internet_shop.service;


import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import ru.yandex.practicum.mvc_internet_shop.model.dto.FilterProductDTO;
import ru.yandex.practicum.mvc_internet_shop.utils.TestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
public class ProductServiceTest {

    @Autowired
    private TestUtils testUtils;

    @Autowired
    private ProductService productService;

    @BeforeAll
    void setupAll() {
        // Добавление тестовых данных
        testUtils.executeSQL("/sql/insert_data_to_bd.sql");
    }

    @AfterAll
    void setDownAll() {
        // Удаление тестовых данных
        testUtils.executeSQL("/sql/clear_data_to_bd.sql");
    }

    // получение списка товаров по фильтрам
    @Test
    void getProductsByFilter_shouldReturnListProduct() {
        FilterProductDTO filter = new FilterProductDTO(0, 10, "", "title asc");
        productService.getProductsByFilter(filter)
                .doOnNext(products -> {
                    assertNotNull(products.getContent());
                    assertEquals(10, products.getContent().size());
                })
                .block();
    }

    // получение товара по идентификатору
    @Test
    void getProductById_shouldReturnProductById() {
        Integer productId = 21;
        productService.getProductById(productId)
                        .doOnSuccess(product -> {
                            assertNotNull(product);
                            assertEquals(productId, product.getId());
                            assertEquals("Детский компьютер обучающий", product.getTitle());
                            assertNotNull(product.getItemId());
                            assertEquals(1, product.getCount());
                        })
                .block();

    }

    // получение товара по идентификатору ен находящегося в корзине
    @Test
    void getProductById_shouldReturnProductByIdWithOutItem() {
        Integer productId = 25;
        productService.getProductById(productId).doOnSuccess(product -> {
                    assertNotNull(product);
                    assertEquals(productId, product.getId());
                    assertNull(product.getItemId());
                    assertNull(product.getCount());
        }).block();

    }
}
