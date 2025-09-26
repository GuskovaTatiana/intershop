package ru.yandex.practicum.shop.controller;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import reactor.core.publisher.Mono;
import ru.yandex.practicum.shop.model.dto.OrderDTO;
import ru.yandex.practicum.shop.model.enums.OrderStatus;
import ru.yandex.practicum.shop.service.OrderService;
import ru.yandex.practicum.shop.service.UserService;
import ru.yandex.practicum.shop.utils.TestDataUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;


@WebFluxTest(CartController.class)
public class CartControllerTest {

    @Autowired
    private WebTestClient webTestClient;
    @MockitoBean
    private OrderService orderService;
    @MockitoBean
    private UserService userService;

    private TestDataUtils testData = new TestDataUtils();

    //Добавление продукта в корзину с редиректом на страницу Каталог
    @Test
    void addProductToOrder_withAuthentication_shouldRedirectToHtmlWithListProduct() throws Exception  {
        Integer productId = 1;
        Mockito.when(orderService.addProductInCart(1, productId, 1))
                .thenReturn(Mono.empty()) // первый вызов - успех
                .thenReturn(Mono.error(new RuntimeException("Service error")));// второй вызов - ошибка
        Mockito.when(userService.findByLogin(any()))
                .thenReturn(Mono.just(testData.getUserTestData()));
        webTestClient.mutateWith(SecurityMockServerConfigurers.mockUser("test"))
                .mutateWith(SecurityMockServerConfigurers.csrf())
                .post().uri("/cart/{productId}/addItem", 1)
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/product?page=0&size=10&sort=title%20asc");

        // Verify
        Mockito.verify(orderService, Mockito.times(1)).addProductInCart(1, productId, 1);
    }

    //Добавление продукта в корзину с редиректом на страницу Каталог
    @Test
    void addProductToOrder_withoutAuthentication_shouldReturnForbidden() throws Exception  {
        Integer productId = 1;
        Mockito.when(orderService.addProductInCart(1, productId, 1))
                .thenReturn(Mono.empty()) // первый вызов - успех
                .thenReturn(Mono.error(new RuntimeException("Service error"))); // второй вызов - ошибка
        Mockito.when(userService.findByLogin(any()))
                .thenReturn(Mono.just(testData.getUserTestData()));
        webTestClient.post().uri("/cart/{productId}/addItem", 1)
                .exchange()
                .expectStatus().isForbidden();
    }


    //Добавление продукта в корзину с редиректом на страницу Товар
    @Test
    void addProductToOrder_withAuthentication_shouldRedirectToHtmlWithProductId() throws Exception  {
        Integer productId = 1;
        String redirectTo = "product/{productId}";
        // мокаем сервис
        Mockito.when(orderService.addProductInCart(1,productId, 1))
                .thenReturn(Mono.empty()) // первый вызов - успех
                .thenReturn(Mono.error(new RuntimeException("Service error"))); // второй вызов - ошибка
        Mockito.when(userService.findByLogin(any()))
                .thenReturn(Mono.just(testData.getUserTestData()));
        webTestClient.mutateWith(SecurityMockServerConfigurers.mockUser("test"))
                .mutateWith(SecurityMockServerConfigurers.csrf())
                .post().uri("/cart/{productId}/addItem?redirectTo={redirectTo}", productId, redirectTo)
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/product/" + productId);
    }

    //Открытие страницы Корзина
    @Test
    void getCart_shouldReturnHtmlWithListProductInCart() throws Exception  {
        OrderDTO order = testData.getOrder(1, OrderStatus.CREATE, testData.getListProduct().getContent());
        Mockito.when(orderService.getOrderInCart(any())).thenReturn(Mono.just(order));
        Mockito.when(orderService.getBalance(any())).thenReturn(Mono.just(20000));
        Mockito.when(userService.findByLogin(any()))
                .thenReturn(Mono.just(testData.getUserTestData()));
        webTestClient.mutateWith(SecurityMockServerConfigurers.mockUser("test"))
                .mutateWith(SecurityMockServerConfigurers.csrf())
                .get().uri("/cart" )
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
                    Elements orderElements = doc.select("#products-container > div");
                    Assertions.assertEquals(3, orderElements.size());


                    assertTrue(body.contains("products"));
                    assertTrue(body.contains("total"));
                });
    }

    //Изменение количества товара в корзине с редиректом на страницу Каталог
    @Test
    void editCountProductFromOrder_shouldRedirectToHtmlWithListProduct() throws Exception  {
        Integer productId = 1;
        Integer itemId = 1;
        Mockito.when(orderService.editProductInOrder(eq(itemId), any(Integer.class), any(Authentication.class)))
                .thenReturn(Mono.just(productId)) // первый вызов - успех
                .thenReturn(Mono.error(new RuntimeException("Service error"))); // второй вызов - ошибка
        webTestClient.mutateWith(SecurityMockServerConfigurers.mockUser("test"))
                .mutateWith(SecurityMockServerConfigurers.csrf())
                .post().uri("/cart/item/{id}/edit?quantity=1", 1)
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/product?page=0&size=10&sort=title%20asc");

        // Verify
        Mockito.verify(orderService, Mockito.times(1)).editProductInOrder(eq(itemId), any(Integer.class), any(Authentication.class));
    }

    //Изменение количества товара в корзине с редиректом на страницу Товар
    @Test
    void editCountProductFromOrder_shouldRedirectToHtmlWithProductId() throws Exception  {
        Integer productId = 1;
        String redirectTo = "product/{productId}";
        Integer itemId = 1;
        // мокаем сервис
        Mockito.when(orderService.editProductInOrder(eq(itemId), any(Integer.class), any(Authentication.class)))
                .thenReturn(Mono.just(productId)) // первый вызов - успех
                .thenReturn(Mono.error(new RuntimeException("Service error"))); // второй вызов - ошибка

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockUser("test"))
                .mutateWith(SecurityMockServerConfigurers.csrf())
                .post().uri("/cart/item/{id}/edit?quantity=1&redirectTo={redirectTo}", itemId, redirectTo)
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/product/" + productId);
    }


    //Удаление товара из корзины с редиректом на страницу Каталог
    @Test
    void deleteProductFromOrder_shouldRedirectToHtmlWithListProduct() throws Exception  {
        Integer productId = 1;
        Integer itemId = 1;
        Mockito.when(orderService.deleteProductInOrder(eq(itemId), any(Authentication.class)))
                .thenReturn(Mono.just(productId)) // первый вызов - успех
                .thenReturn(Mono.error(new RuntimeException("Service error"))); // второй вызов - ошибка
        webTestClient.mutateWith(SecurityMockServerConfigurers.mockUser("test"))
                .mutateWith(SecurityMockServerConfigurers.csrf())
                .post().uri("/cart/item/{id}/delete", 1)
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/product?page=0&size=10&sort=title%20asc");
    }

    //Удаление товара из корзины с редиректом на страницу Товар
    @Test
    void deleteProductFromOrder_shouldRedirectToHtmlWithProductId() throws Exception  {
        Integer productId = 1;
        String redirectTo = "product/{productId}";
        Integer itemId = 1;
        // мокаем сервис
        Mockito.when(orderService.deleteProductInOrder(eq(itemId), any(Authentication.class)))
                .thenReturn(Mono.just(productId)) // первый вызов - успех
                .thenReturn(Mono.error(new RuntimeException("Service error"))); // второй вызов - ошибка

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockUser("test"))
                .mutateWith(SecurityMockServerConfigurers.csrf())
                .post().uri("/cart/item/{id}/delete?redirectTo={redirectTo}", itemId, redirectTo)
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/product/" + productId);
    }

}
