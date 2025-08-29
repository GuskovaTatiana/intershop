package ru.yandex.practicum.mvc_internet_shop.service;


import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.mvc_internet_shop.mapper.ProductMapper;
import ru.yandex.practicum.mvc_internet_shop.model.dto.FilterProductDTO;
import ru.yandex.practicum.mvc_internet_shop.model.dto.ProductDTO;
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
        Page<ProductDTO> products = productService.getProductsByFilter(filter);

        assertNotNull(products.getContent());
        assertEquals(10, products.getContent().size());
    }


    // получение товара по идентификатору
    @Test
    void getProductById_shouldReturnProductById() {
        ProductDTO product = productService.getProductById(21);
        assertNotNull(product);
        assertEquals(21, product.getId());
        assertEquals("Детский компьютер обучающий", product.getTitle());
        assertNotNull(product.getItemId());
        assertEquals(1, product.getCount());
    }

    // получение товара по идентификатору
    @Test
    void getProductById_shouldReturnProductByIdWithOutItem() {
        ProductDTO product = productService.getProductById(1);
        assertNotNull(product);
        assertEquals(1, product.getId());
        assertNull(product.getItemId());
        assertNull(product.getCount());
    }
}
