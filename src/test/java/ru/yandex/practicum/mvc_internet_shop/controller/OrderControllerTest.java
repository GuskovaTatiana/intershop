package ru.yandex.practicum.mvc_internet_shop.controller;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mvc_internet_shop.model.dto.OrderDTO;
import ru.yandex.practicum.mvc_internet_shop.model.enums.OrderStatus;
import ru.yandex.practicum.mvc_internet_shop.service.OrderService;
import ru.yandex.practicum.mvc_internet_shop.utils.TestDataUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@WebFluxTest(OrderController.class)
public class OrderControllerTest {

    @Autowired
    private WebTestClient webTestClient;
    @MockitoBean
    private OrderService orderService;
    private TestDataUtils testData = new TestDataUtils();

    //Оформление заказа
    @Test
    void createOrder_shouldReturnHtmlWithOrderInfo() throws Exception  {
        OrderDTO createOrder = testData.getOrder(1, OrderStatus.CLOSED, testData.getListProduct().getContent());
        Mockito.when(orderService.addNewOrder())
                .thenReturn(Mono.just(createOrder));
        webTestClient.post().uri("/orders")
                .exchange()
                .expectStatus().isFound()
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
                    assertEquals(3, orderElements.size());


                    Element firstOrder = orderElements.first();
                    assertNotNull(firstOrder);
                    // Проверка титульника заказа
                    Element titleElement = firstOrder.selectFirst("div > a");
                    assertNotNull(titleElement);
                    assertEquals("Заказ №1", titleElement.text());

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
                    assertEquals("Заказ № 1", orderElements.text());

                    assertTrue(body.contains("orders"));
                });
    }
}
