package ru.yandex.practicum.shop.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.shop.model.User;
import ru.yandex.practicum.shop.model.dto.OrderDTO;
import ru.yandex.practicum.shop.model.dto.ProductDTO;
import ru.yandex.practicum.shop.model.enums.OrderStatus;
import ru.yandex.practicum.shop.utils.TestDataUtils;
import ru.yandex.practicum.shop.utils.TestUtils;

import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
@ActiveProfiles("test")
public class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @MockitoBean
    private PaymentServiceClient paymentService;

    @Autowired
    private TestUtils testUtils;

    @Autowired
    private TestDataUtils testData;

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
        var authUser = new UsernamePasswordAuthenticationToken("test", "test");
        userService.findByLogin(authUser.getName()).flatMap(user -> {
            return orderService.findAllCompletedOrder(user.getId())
                    .collectList();
                })
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

        var authUser = new UsernamePasswordAuthenticationToken("test", "test");
        userService.findByLogin(authUser.getName()).flatMap(user -> orderService.getOrderInCart(user.getId()))
                .doOnSuccess(createOrder -> {
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
        Mockito.when(paymentService.processPayment(any(), any()))
                .thenReturn(Mono.just(testData.getPaymentResponse(null)));
        var authUser = new UsernamePasswordAuthenticationToken("test", "test");
        User user = userService.findByLogin(authUser.getName()).block();
        OrderDTO result = orderService.getOrderInCart(user.getId())
                .flatMap(orderInCart -> {
                    // Assert pre-conditions
                    assertNotNull(orderInCart, "Заказ в корзине не должен быть null");

                    return orderService.addNewOrder(user.getId(), orderInCart.getTotalPrice())
                            .map(orderInProgress -> {
                                // Assert post-conditions
                                assertNotNull(orderInProgress, "Заказ в процессе не должен быть null");
                                assertEquals(orderInCart.getId(), orderInProgress.getId(),
                                        "ID заказа должны совпадать");
                                assertNotEquals(orderInCart.getStatus(), orderInProgress.getStatus(),
                                        "Статус заказа должен измениться");
                                assertEquals(orderInCart.getProducts().size(), orderInProgress.getProducts().size(),
                                        "Количество продуктов должно остаться неизменным");
                                assertEquals(orderInCart.getTotalPrice(), orderInProgress.getTotalPrice(),
                                        "Общая цена должна остаться неизменной");
                                return orderInProgress;
                            });
                })
                .block(); // Извлекаем результат из Mono

        assertNotNull(result, "Результат не должен быть null");
    }

    //Создание нового заказа для заполнения
    @Test
    void findOrCreateOrderInCart_shouldCreateAndReturnOrderInStatusCreate() {
        testUtils.executeSQL("/sql/reset-sequences.sql");
        var authUser = new UsernamePasswordAuthenticationToken("test", "test");
        User user = userService.findByLogin(authUser.getName()).block();
        Mockito.when(paymentService.processPayment(any(), any()))
                .thenReturn(Mono.just(testData.getPaymentResponse(null)));
        //Переводим предыдущий заказ в статус оформлен
        orderService.addNewOrder(user.getId(), 100)
                //создаем новый заказ
                .then(
                        orderService.getOrCreateCartOrderWithCleanup(user.getId())
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
        Mockito.when(paymentService.processPayment(any(), any()))
                .thenReturn(Mono.just(testData.getPaymentResponse(null)));
        var authUser = new UsernamePasswordAuthenticationToken("test", "test");
        User user = userService.findByLogin(authUser.getName()).block();
        //Переводим предыдущий заказ в статус оформлен
        orderService.addNewOrder(user.getId(), 100).block();

        orderService.findById(3, user.getId())
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
        Mockito.when(paymentService.processPayment(any(), any()))
                .thenReturn(Mono.just(testData.getPaymentResponse(null)));
        var authUser = new UsernamePasswordAuthenticationToken("test", "test");
        User user = userService.findByLogin(authUser.getName()).block();
        testUtils.executeSQL("/sql/reset-sequences.sql");
        orderService.addNewOrder(user.getId(),100).block();

        Integer productId = 21;
        // Получаем продукт и выполняем операции
        ProductDTO product = orderService.getOrderInCart(user.getId())
                .flatMap(cart -> productService.getProductById(cart.getId(), productId))
                .doOnNext(p -> {
                    assertNotNull(p, "Продукт не должен быть null");
                })
                .block();

        assertNotNull(product, "Продукт должен быть найден");

        // Добавляем продукт в корзину
        orderService.addProductInCart(user.getId(), productId, 1).block();

        // Получаем заказ в корзине и проверяем
        OrderDTO orderInCart = orderService.getOrderInCart(user.getId()).block();

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
        Authentication authUser = new UsernamePasswordAuthenticationToken("test", "test", Collections.emptyList());
        User user = userService.findByLogin(authUser.getName()).block();
        // получаем продукт по идентификатору
        ProductDTO product = orderService.getOrderInCart(user.getId())
                .flatMap(cart -> productService.getProductById(cart.getId(), productId))
                .doOnNext(p -> {
                    assertNotNull(p, "Продукт не должен быть null");
                    assertNotNull(p.getItemId());
                })
                .block();

        // меняем количество продукта в корзине
        Integer editProductId = orderService.editProductInOrder(product.getItemId(), quantity, authUser)
                .doOnNext(id -> assertEquals(product.getId(), id))
                .block();

        // получаем заказ находящийся в корзине и проверяем есть ли в ней такой товар
        OrderDTO createOrder = orderService.getOrderInCart(user.getId())
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

        Authentication authUser = new UsernamePasswordAuthenticationToken("test", "test", Collections.emptyList());
        User user = userService.findByLogin(authUser.getName()).block();

        // получаем продукт по идентификатору
        ProductDTO product = productService.getProductById(null, productId)
                .doOnNext(p -> assertNotNull(p)).block();

        //добавляем продукт в корзину
        orderService.addProductInCart(user.getId(), productId, 1).block();
        // проверяем что продукт в корзине
        OrderDTO order = orderService.getOrderInCart(user.getId())
                .doOnNext(o -> {
                    assertNotNull(o);
                    assertNotNull(o.getProducts());
                    assertTrue(o.getProducts().stream().map(ProductDTO:: getId).toList().contains(productId));
                }).block();


        ProductDTO prod = order.getProducts().stream().filter(it -> it.getId().equals(productId)).findFirst().get();
        assertNotNull(prod);
        // удаляем продукт из корзины
        Integer prodtId = orderService.deleteProductInOrder(prod.getItemId(), authUser).block();

        // проверяем что продукта нет в корзине
        OrderDTO newOrder = orderService.getOrderInCart(user.getId())
                .doOnNext(o -> assertNotNull(o)).block();
        if (newOrder.getProducts() != null) {
            assertFalse(newOrder.getProducts().stream().map(ProductDTO:: getId).toList().contains(productId));
        }
    }
}
