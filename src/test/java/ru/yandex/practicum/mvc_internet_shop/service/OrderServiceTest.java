package ru.yandex.practicum.mvc_internet_shop.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.mvc_internet_shop.model.dto.OrderDTO;
import ru.yandex.practicum.mvc_internet_shop.model.dto.ProductDTO;
import ru.yandex.practicum.mvc_internet_shop.model.enums.OrderStatus;
import ru.yandex.practicum.mvc_internet_shop.utils.TestUtils;

import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
public class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductService productService;

    @Autowired
    private TestUtils testUtils;

    @BeforeEach
    void setup() {
        // Добавление тестовых данных
        testUtils.executeSQL("/sql/insert_data_to_bd.sql");
    }

    @AfterEach
    void setDown() {
        // Удаление тестовых данных
        testUtils.executeSQL("/sql/clear_data_to_bd.sql");
    }

    // Получение списка оформленных заказов
    @Test
    void findAllCompletedOrder_shouldReturnListOrderDTO() {
        orderService.findAllCompletedOrder()
                .collectList()
                .doOnSuccess(allCompletedOrders -> {
                    assertNotNull(allCompletedOrders);
                    assertEquals(2, allCompletedOrders.size());
                    OrderDTO closedOrder = allCompletedOrders.stream().filter(it -> it.getStatus().equals(OrderStatus.CLOSED)).findFirst().get();

                    assertNotNull(closedOrder);
                    assertEquals(1, closedOrder.getId());
                    assertEquals(OrderStatus.CLOSED, closedOrder.getStatus());
                    assertEquals(2, closedOrder.getProducts().size());
                }).block();
    }

    // Получение не оформленного заказа (из корзины)
    @Test
    void getOrderInCart_shouldReturnOrderDTOInStatusCreate() {
        orderService.getOrderInCart().doOnSuccess(createOrder -> {
                    assertNotNull(createOrder);
                    assertEquals(3, createOrder.getId());
                    assertEquals(OrderStatus.CREATE, createOrder.getStatus());
                    assertEquals(4, createOrder.getProducts().size());
                    assertEquals(createOrder.getTotalPrice(), createOrder.getProducts().stream().map(ProductDTO::getTotalPrice).reduce(0, Integer::sum));
                }).block();
    }

    // перевод заказа из статуса CREATE в статус IN_PROGRESS
    @Test
    void addNewOrder_shouldChangeStatusAndReturnOrderDTO() {
        orderService.getOrderInCart()
                .flatMap(orderInCart -> {
                    assertNotNull(orderInCart, "Заказ в корзине не должен быть null");
                    return orderService.addNewOrder()
                            .map(orderInProgress -> {
                                assertNotNull(orderInProgress, "Заказ в процессе не должен быть null");
                                assertEquals(orderInProgress.getId(), orderInCart.getId());
                                assertNotEquals(orderInProgress.getStatus(), orderInCart.getStatus());
                                assertEquals(orderInProgress.getProducts().size(), orderInCart.getProducts().size());
                                assertEquals(orderInCart.getTotalPrice(), orderInProgress.getTotalPrice());
                                return orderInProgress;
                            });
                }).block();
    }

    //Создание нового заказа для заполнения
    @Test
    void findOrCreateOrderInCart_shouldCreateAndReturnOrderInStatusCreate() {
        testUtils.executeSQL("/sql/reset-sequences.sql");
        //Переводим предыдущий заказ в статус оформлен
        orderService.addNewOrder()
                //создаем новый заказ
                .then(
                        orderService.getOrCreateCartOrderWithCleanup()
                                    .doOnSuccess(createOrder -> {
                                        assertNotNull(createOrder);
                                        assertTrue(createOrder.getProducts().isEmpty());
                                        assertEquals(OrderStatus.CREATE, createOrder.getStatus());
                                    })
                        ).block();
    }

    // Получение заказа по идентификатору
    @Test
    void findById_shouldCreateAndReturnOrderInStatusCreate() {
        //Переводим предыдущий заказ в статус оформлен
        orderService.addNewOrder().block();

        orderService.findById(3)
                .doOnSuccess(order -> {
                    assertNotNull(order);
                    assertEquals(3, order.getId());
                    assertEquals(OrderStatus.IN_PROGRESS, order.getStatus());
                    assertEquals(4, order.getProducts().size());
                }
        ).block();
    }

    //Добавление/Обновление товара в корзине
    @Test
    void addProductInCart_shouldAddProductInCart() {
        //Переводим предыдущий заказ в статус оформлен
        testUtils.executeSQL("/sql/reset-sequences.sql");
        orderService.addNewOrder().block();

        Integer productId = 21;
        // Получаем продукт и выполняем операции
        ProductDTO product = productService.getProductById(productId)
                .doOnNext(p -> assertNotNull(p, "Продукт не должен быть null"))
                .block();

        assertNotNull(product, "Продукт должен быть найден");

        // Добавляем продукт в корзину
        orderService.addProductInCart(productId, 1).block();

        // Получаем заказ в корзине и проверяем
        OrderDTO orderInCart = orderService.getOrderInCart().block();

        assertNotNull(orderInCart, "Заказ в корзине не должен быть null");
        assertNotNull(orderInCart.getProducts(), "Список продуктов не должен быть null");

        // Ищем добавленный продукт
        List<ProductDTO> prodList = orderInCart.getProducts().stream()
                .filter(it -> it.getId().equals(productId))
                .toList();

        assertFalse(prodList.isEmpty(), "Продукт должен быть добавлен в корзину");

        ProductDTO dto = prodList.get(0);
        assertEquals(product.getId(), dto.getId(), "ID продукта должны совпадать");
        assertEquals(product.getTitle(), dto.getTitle(), "Названия продуктов должны совпадать");
        assertEquals(1, dto.getCount(), "Количество должно быть 1");
        assertNotNull(dto.getItemId(), "ItemId не должен быть null");
    }

    //Изменение количества товара из корзины
    @Test
    void editProductInOrder_shouldChangeCountProductAddReturnProductId() {
        Integer productId = 21;
        Integer quantity = 5;
        // получаем продукт по идентификатору
        ProductDTO product = productService.getProductById(productId)
                .doOnNext(p -> {
                    assertNotNull(p, "Продукт не должен быть null");
                    assertNotNull(p.getItemId());
                })
                .block();

        // меняем количество продукта в корзине
        Integer editProductId = orderService.editProductInOrder(product.getItemId(), quantity)
                .doOnNext(id -> assertEquals(product.getId(), id))
                .block();

        // получаем заказ находящийся в корзине и проверяем есть ли в ней такой товар
        OrderDTO createOrder = orderService.getOrderInCart()
                .doOnNext(order -> {
                    assertNotNull(order);
                    assertNotNull(order.getProducts());
                }).block();

        List<ProductDTO> prodList = createOrder.getProducts().stream().filter(it -> it.getId().equals(productId)).toList();
        assertNotNull(prodList);
        assertFalse(prodList.isEmpty());
        ProductDTO dto = prodList.stream().findFirst().get();

        // сравниваем параметры товара: было стало
        assertEquals(product.getId(), dto.getId());
        assertEquals(product.getTitle(), dto.getTitle());
        assertEquals(product.getCount() + quantity, dto.getCount());
        assertEquals(product.getItemId(), dto.getItemId());
    }

    //Удаление товара из корзины
    @Test
    void deleteProductInOrder_shouldChangeCountProductAddReturnProductId() {
        testUtils.executeSQL("/sql/reset-sequences.sql");
        Integer productId = 22;
        // получаем продукт по идентификатору
        ProductDTO product = productService.getProductById(productId)
                .doOnNext(p -> assertNotNull(p)).block();

        //добавляем продукт в корзину
        orderService.addProductInCart(productId, 1).block();
        // проверяем что продукт в корзине
        OrderDTO order = orderService.getOrderInCart()
                .doOnNext(o -> {
                    assertNotNull(o);
                    assertNotNull(o.getProducts());
                    assertTrue(o.getProducts().stream().map(ProductDTO:: getId).toList().contains(productId));
                }).block();


        ProductDTO prod = order.getProducts().stream().filter(it -> it.getId().equals(productId)).findFirst().get();
        assertNotNull(prod);
        // удаляем продукт из корзины
        Integer prodtId = orderService.deleteProductInOrder(prod.getItemId()).block();

        // проверяем что продукта нет в корзине
        OrderDTO newOrder = orderService.getOrderInCart()
                .doOnNext(o -> assertNotNull(o)).block();
        if (newOrder.getProducts() != null) {
            assertFalse(newOrder.getProducts().stream().map(ProductDTO:: getId).toList().contains(productId));
        }
    }
}
