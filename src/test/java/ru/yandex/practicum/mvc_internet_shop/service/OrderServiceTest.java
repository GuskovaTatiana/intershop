package ru.yandex.practicum.mvc_internet_shop.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.mvc_internet_shop.model.Order;
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
        List<OrderDTO> allCompletedOrders = orderService.findAllCompletedOrder();
        assertNotNull(allCompletedOrders);
        assertEquals(2, allCompletedOrders.size());
        OrderDTO closedOrder = allCompletedOrders.stream().filter(it -> it.getStatus().equals(OrderStatus.CLOSED)).findFirst().get();

        assertNotNull(closedOrder);
        assertEquals(1, closedOrder.getId());
        assertEquals(OrderStatus.CLOSED, closedOrder.getStatus());
        assertEquals(2, closedOrder.getProducts().size());
    }

    // Получение не оформленного заказа (из корзины)
    @Test
    void getOrderInCart_shouldReturnOrderDTOInStatusCreate() {
        OrderDTO createOrder = orderService.getOrderInCart();
        assertNotNull(createOrder);
        assertEquals(3, createOrder.getId());
        assertEquals(OrderStatus.CREATE, createOrder.getStatus());
        assertEquals(4, createOrder.getProducts().size());
        assertEquals(createOrder.getTotalPrice(), createOrder.getProducts().stream().map(ProductDTO::getTotalPrice).reduce(0, Integer::sum));
    }

    // перевод заказа из статуса CREATE в статус IN_PROGRESS
    @Test
    void addNewOrder_shouldChangeStatusAndReturnOrderDTO() {
        OrderDTO createOrder = orderService.getOrderInCart();
        OrderDTO orderInProgress = orderService.addNewOrder();
        assertNotNull(createOrder);
        assertNotNull(orderInProgress);
        assertEquals(orderInProgress.getId(), createOrder.getId());
        assertNotEquals(orderInProgress.getStatus(), createOrder.getStatus());
        assertEquals(orderInProgress.getProducts().size(), createOrder.getProducts().size());
        assertEquals(createOrder.getTotalPrice(), orderInProgress.getTotalPrice());
    }

    //Создание нового заказа для заполнения
    @Test
    void findOrCreateOrderInCart_shouldCreateAndReturnOrderInStatusCreate() {
        //Переводим предыдущий заказ в статус оформлен
        orderService.addNewOrder();
        //создаем новый заказ
        Order createOrder = orderService.findOrCreateOrderInCart();
        assertNotNull(createOrder);
        assertTrue(createOrder.getProducts().isEmpty());
        assertEquals(OrderStatus.CREATE, createOrder.getStatus());
    }

    // Получение заказа по идентификатору
    @Test
    void findById_shouldCreateAndReturnOrderInStatusCreate() {
        //Переводим предыдущий заказ в статус оформлен
        orderService.addNewOrder();

        OrderDTO order = orderService.findById(3);
        assertNotNull(order);
        assertEquals(3, order.getId());
        assertEquals(OrderStatus.IN_PROGRESS, order.getStatus());
        assertEquals(4, order.getProducts().size());
    }

    //Добавление/Обновление товара в корзине
    @Test
    void addProductInCart_shouldAddProductInCart() {
        //Переводим предыдущий заказ в статус оформлен
        orderService.addNewOrder();

        Integer productId = 21;
        ProductDTO product = productService.getProductById(productId);
        assertNotNull(product);
        orderService.addProductInCart(productId, 1);
        OrderDTO createOrder = orderService.getOrderInCart();
        assertNotNull(createOrder);
        assertNotNull(createOrder.getProducts());
        List<ProductDTO> prodList = createOrder.getProducts().stream().filter(it -> it.getId().equals(productId)).toList();
        assertNotNull(prodList);
        assertFalse(prodList.isEmpty());
        ProductDTO dto = prodList.stream().findFirst().get();

        assertEquals(product.getId(), dto.getId());
        assertEquals(product.getTitle(), dto.getTitle());
        assertEquals(1, dto.getCount());
        assertNotNull(dto.getItemId());
    }

    //Изменение количества товара из корзины
    @Test
    void editProductInOrder_shouldChangeCountProductAddReturnProductId() {
        Integer productId = 21;
        Integer quantity = 5;
        // получаем продукт по идентификатору
        ProductDTO product = productService.getProductById(productId);
        assertNotNull(product);
        assertNotNull(product.getItemId());

        // меняем количество продукта в корзине
        Integer editProductId = orderService.editProductInOrder(product.getItemId(), quantity);
        assertEquals(product.getId(), editProductId);

        // получаем заказ находящийся в корзине и проверяем есть ли в ней такой товар
        OrderDTO createOrder = orderService.getOrderInCart();
        assertNotNull(createOrder);
        assertNotNull(createOrder.getProducts());
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
        Integer productId = 22;
        // получаем продукт по идентификатору
        ProductDTO product = productService.getProductById(productId);
        assertNotNull(product);
        //добавляем продукт в корзину
        orderService.addProductInCart(productId, 1);
        // проверяем что продукт в корзине
        OrderDTO createOrder = orderService.getOrderInCart();
        assertNotNull(createOrder);
        assertNotNull(createOrder.getProducts());
        assertTrue(createOrder.getProducts().stream().map(ProductDTO:: getId).toList().contains(productId));

        ProductDTO prod = createOrder.getProducts().stream().filter(it -> it.getId().equals(productId)).findFirst().get();
        assertNotNull(prod);
        // удаляем продукт из корзины
        orderService.deleteProductInOrder(prod.getItemId());

        // проверяем что продукта нет в корзине
        createOrder = orderService.getOrderInCart();
        assertNotNull(createOrder);
        if (createOrder.getProducts() != null) {
            assertFalse(createOrder.getProducts().stream().map(ProductDTO:: getId).toList().contains(productId));
        }
    }
}
