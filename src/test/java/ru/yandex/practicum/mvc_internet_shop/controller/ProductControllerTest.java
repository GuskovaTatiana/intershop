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
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mvc_internet_shop.model.dto.ProductDTO;
import ru.yandex.practicum.mvc_internet_shop.service.ProductService;
import ru.yandex.practicum.mvc_internet_shop.utils.TestDataUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

@WebFluxTest(ProductController.class)
public class ProductControllerTest {

    @Autowired
    private WebTestClient webTestClient;
    @MockitoBean
    private ProductService productService;

    private TestDataUtils testData = new TestDataUtils();

    //Получение списка продуктов
    @Test
    void getListOfProducts_shouldReturnHtmlWithProducts() throws Exception  {
        Mockito.when(productService.getProductsByFilter(any())).thenReturn(Mono.just(testData.getListProduct()));
        webTestClient.get().uri(uriBuilder -> uriBuilder
                        .path("/product")
                        .queryParam("page", "0")
                        .queryParam("size", "10")
                        .build())
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
                    Elements productContainerElements = doc.select("#products-container > div");
                    assertEquals(3, productContainerElements.size());

                    Element product = productContainerElements.first();
                    assertNotNull(product);
                    // Проверка титульника заказа
                    Element titleElement = product.selectFirst("div > a > h3");
                    assertNotNull(titleElement);
                    assertEquals("Товар 1", titleElement.text());

                    assertTrue(body.contains("products"));
                });
    }

    //Открытие страницы с описанием продукта
    @Test
    void getProductById_shouldReturnHtmlWithProductInfo() throws Exception  {
        Integer productId = 3;
        ProductDTO dto = testData.getProduct(productId, "Товар 3", "/images/image3.png", "Описание 3", 25);
        Mockito.when(productService.getProductById(productId)).thenReturn(Mono.just(dto));
        webTestClient.get().uri("/product/{productId}", 3)
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
                    Elements itemElements = doc.select("#item");
                    assertEquals(1, itemElements.size());

                    // Проверка титульника заказа
                    Element titleElement = itemElements.selectFirst("h3");
                    assertNotNull(titleElement);
                    assertEquals("Товар 3", titleElement.text());

                    assertTrue(body.contains("product"));
                });
    }
}
