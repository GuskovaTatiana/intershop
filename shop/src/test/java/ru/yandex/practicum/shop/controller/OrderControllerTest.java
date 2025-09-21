package ru.yandex.practicum.shop.controller;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.shop.model.dto.OrderDTO;
import ru.yandex.practicum.shop.model.enums.OrderStatus;
import ru.yandex.practicum.shop.service.OrderService;
import ru.yandex.practicum.shop.service.PaymentServiceClient;
import ru.yandex.practicum.shop.utils.TestDataUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

@WebFluxTest(OrderController.class)
public class OrderControllerTest {

    @Autowired
    private WebTestClient webTestClient;
    @MockitoBean
    private OrderService orderService;
    private TestDataUtils testData = new TestDataUtils();

    @MockitoBean
    private PaymentServiceClient paymentService;

    //Оформление заказа
    @Test
    void createOrder_shouldReturnHtmlWithOrderInfo() throws Exception  {
        Mockito.when(paymentService.processPayment(any()))
                .thenReturn(Mono.just(testData.getPaymentResponse(null)));
        OrderDTO createOrder = testData.getOrder(1, OrderStatus.CLOSED, testData.getListProduct().getContent());
        Mockito.when(orderService.addNewOrder(any()))
                .thenReturn(Mono.just(createOrder));
        webTestClient.post().uri("/orders?amount=100")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/orders/1");

    }

    //Получение списка заказов getOrders
    @Test
    void getOrders_shouldReturnHtmlWithListOrder() throws Exception  {
        List<OrderDTO> createOrder = testData.getListOrder();
        Mockito.when(orderService.findAllCompletedOrder()).thenReturn(Flux.fromIterable(createOrder));
        webTestClient.get().uri("/orders")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .consumeWith(response -> {
                    String body = response.getResponseBody();
                    assertNotNull(body);

                    // Парсинг HTML с Jsoup
                    Document doc = Jsoup.parse(body);

                    // Проверка количества заказов
                    Elements orderElements = doc.select("#order-list > div");
                    Assertions.assertEquals(3, orderElements.size());


                    Element firstOrder = orderElements.first();
                    assertNotNull(firstOrder);
                    // Проверка титульника заказа
                    Element titleElement = firstOrder.selectFirst("div > a");
                    assertNotNull(titleElement);
                    Assertions.assertEquals("Заказ №1", titleElement.text());

                    assertTrue(body.contains("orders"));
                });
    }

    //Получение заказа по идентификатору getOrders
    @Test
    void getOrderById_shouldReturnHtmlWithOrderInfo() throws Exception  {
        OrderDTO createOrder = testData.getOrder(1, OrderStatus.CLOSED, testData.getListProduct().getContent());
        Mockito.when(orderService.findById(1)).thenReturn(Mono.just(createOrder));
        webTestClient.get().uri("/orders/{orderId}", 1)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .consumeWith(response -> {
                    String body = response.getResponseBody();
                    assertNotNull(body);

                    // Парсинг HTML с Jsoup
                    Document doc = Jsoup.parse(body);

                    // Проверка количества заказов
                    Elements orderElements = doc.select("#order-title > a");
                    Assertions.assertEquals("Заказ № 1", orderElements.text());

                    assertTrue(body.contains("orders"));
                });
    }
}
