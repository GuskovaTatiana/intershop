package ru.yandex.practicum.shop.service;


import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import ru.yandex.practicum.shop.model.dto.FilterProductDTO;
import ru.yandex.practicum.shop.utils.TestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
public class ProductServiceTest {

    @Autowired
    private TestUtils testUtils;

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private CacheService cacheService;

    @BeforeAll
    void setupAll() {
        // Добавление тестовых данных
        testUtils.executeSQL("/sql/insert_data_to_bd.sql");
        cacheService.clearCache().block();
    }

    @AfterAll
    void setDownAll() {
        // Удаление тестовых данных
        testUtils.executeSQL("/sql/clear_data_to_bd.sql");
        cacheService.clearCache().block();
    }

    // получение списка товаров по фильтрам
    @Test
    void getProductsByFilter_shouldReturnListProduct() {
        FilterProductDTO filter = new FilterProductDTO(0, 10, "", "title asc");
        productService.getProductsByFilter(any(), filter)
                .doOnNext(products -> {
                    assertNotNull(products.getContent());
                    assertEquals(10, products.getContent().size());
                })
                .block();
    }

    // получение списка товаров с проверкой наличия кэша
    @Test
    void getProductsByFilter_shouldReturnListProductAndUseCache() {
        // 17 загружаются автоматом + 5 через sql
        Integer productSize = 17 + 5;
        FilterProductDTO filter = new FilterProductDTO(0, 10, "", "title asc");
        productService.getProductsByFilter(any(), filter).block();

        cacheService.getListProductFromCache()
                .doOnNext(products -> {
                    assertNotNull(products);
                    assertEquals(productSize, products.size());
                }).block();

    }

    // получение товара по идентификатору с проверкой кэширования
    @Test
    void getProductById_shouldReturnProductByIdAndUseCache() {
        Integer productId = 21;

        // Первый вызов - должен загрузить из БД и сохранить в кэш
        userService.findByLogin("test")
                .flatMap(authUser -> orderService.getOrderInCart(authUser.getId()))
                .flatMap(order -> productService.getProductById(order.getId(), productId))
                .doOnSuccess(product -> {
                    assertNotNull(product);
                    assertEquals(productId, product.getId());
                    assertEquals("Детский компьютер обучающий", product.getTitle());
                    assertNotNull(product.getItemId());
                    assertEquals(1, product.getCount());
                })
                .block();

        // Второй вызов - должен использовать кэш
        productService.getProductById(any(), productId)
                .doOnSuccess(product -> {
                    assertNotNull(product);
                    assertEquals(productId, product.getId());
                })
                .block();

        // Проверяем, что данные есть в кэше
        cacheService.getProductByIdFromCache(productId).doOnNext(product -> {
            assertNotNull(product);
            assertEquals(productId, product.getId());
        }).block();
    }

    // получение товара по идентификатору
    @Test
    void getProductById_shouldReturnProductById() {
        Integer productId = 21;
        userService.findByLogin("test")
                .flatMap(authUser -> orderService.getOrderInCart(authUser.getId()))
                .flatMap(order -> productService.getProductById(order.getId(), productId))
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
        productService.getProductById(any(), productId).doOnSuccess(product -> {
                    assertNotNull(product);
                    assertEquals(productId, product.getId());
                    assertNull(product.getItemId());
                    assertNull(product.getCount());
        }).block();

    }
}
