package ru.yandex.practicum.payment_service.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
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
public class PaymentControllerTest {

    @Autowired
    private WebTestClient webTestClient;
    @MockitoBean
    private PaymentService paymentService;


    @Test
    void getBalance_ShouldReturnBalance_WhenServiceReturnsValue() {
        // Arrange
        Integer balance = 20000;
        Mockito.when(paymentService.getBalance())
                .thenReturn(Mono.just(balance));
        webTestClient.get().uri("/api/payments/balance")
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

        Mockito.when(paymentService.setBalance(any()))
                .thenReturn(Mono.just(newBalance));

        webTestClient.post().uri("/api/payments/balance")
                .contentType(MediaType.APPLICATION_JSON)
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

        Mockito.when(paymentService.processPayment(any()))
                .thenReturn(Mono.just(transaction));

        webTestClient.post().uri("/api/payments/process")
                .contentType(MediaType.APPLICATION_JSON)
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
