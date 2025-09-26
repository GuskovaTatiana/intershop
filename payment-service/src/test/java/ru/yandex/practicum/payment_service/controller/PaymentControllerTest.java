package ru.yandex.practicum.payment_service.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.model.AmountRequest;
import ru.yandex.practicum.model.BalanceResponse;
import ru.yandex.practicum.model.PaymentBody;
import ru.yandex.practicum.model.PaymentResponse;
import ru.yandex.practicum.payment_service.model.PaymentTransaction;
import ru.yandex.practicum.payment_service.service.PaymentService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

@WebFluxTest(PaymentController.class)
@ActiveProfiles("test")
public class PaymentControllerTest {

    @Autowired
    private WebTestClient webTestClient;
    @MockitoBean
    private PaymentService paymentService;

    @Value("${payment-service.oauth2.client-id}")
    private String clientId;

    @Value("${payment-service.oauth2.client-secret}")
    private String clientSecret;

    private String getTestToken() {
        try {
            WebClient webClient = WebClient.create();

            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("grant_type", "client_credentials");
            formData.add("client_id", clientId);
            formData.add("client_secret", clientSecret); // Замените на реальный секрет

            String response = webClient.post()
                    .uri("http://localhost:8007/realms/ecommerce-realm/protocol/openid-connect/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(formData)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(response);
            return jsonNode.get("access_token").asText();

        } catch (Exception e) {
            // Fallback к статическому токену для разработки
            return "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0LXVzZXIiLCJhdWQiOiJwYXltZW50LXNlcnZpY2UiLCJleHAiOjk5OTk5OTk5OTl9.test-signature";
        }
    }

    @Test
    void getBalance_ShouldReturnBalance_WhenServiceReturnsValue() {
        // Arrange
        Integer balance = 20000;
        Mockito.when(paymentService.getBalance(any()))
                .thenReturn(Mono.just(balance));
        webTestClient.get().uri("/api/payments/1/balance")
                .headers(headers -> headers.setBearerAuth(getTestToken()))
                .exchange()
                .expectStatus().isOk()
                .expectBody(BalanceResponse.class)
                .value(response -> {
                    assertEquals(balance, response.getBalance());
                });
    }

    @Test
    void setBalance_ShouldReturnCreated_WhenDepositSuccessful() {
        Integer newBalance = 5000;
        AmountRequest amountRequest = new AmountRequest();
        amountRequest.setDepositAmount(3000);

        Mockito.when(paymentService.setBalance(any(), any()))
                .thenReturn(Mono.just(newBalance));

        webTestClient.post().uri("/api/payments/1/balance")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> headers.setBearerAuth(getTestToken()))
                .bodyValue(amountRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Void.class);
    }

    @Test
    void processPayment_ShouldReturnPaymentResponse_WhenPaymentSuccessful() {
        // Arrange
        PaymentBody paymentBody = new PaymentBody();
        paymentBody.setAmount(3000);

        PaymentTransaction transaction = PaymentTransaction
                .builder()
                .transactionId("8b15d1ae-48a7-499c-b8c8-37c278158aa5")
                .balance(5000)
                .success(true).build();

        Mockito.when(paymentService.processPayment(any(), any()))
                .thenReturn(Mono.just(transaction));

        webTestClient.post().uri("/api/payments/1/process")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> headers.setBearerAuth(getTestToken()))
                .bodyValue(paymentBody)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(PaymentResponse.class)
                .value(response -> {
                    assertTrue(response.getSuccess());
                    assertEquals(transaction.getTransactionId(), response.getTransactionId());
                });
    }
}
