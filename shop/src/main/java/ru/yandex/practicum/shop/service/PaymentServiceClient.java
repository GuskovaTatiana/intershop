package ru.yandex.practicum.shop.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.api.DefaultApi;

@Component
@RequiredArgsConstructor
public class PaymentServiceClient extends DefaultApi {




//
//    public Mono<PaymentResponse> processPayment(String userId, BigDecimal amount, String orderId) {
//        PaymentRequest request = new PaymentRequest()
//                .userId(userId)
//                .amount(amount)
//                .orderId(orderId);
//
//        return paymentsApi.processPayment(Mono.just(request))
//                .map(ResponseEntity::getBody)
//                .onErrorResume(e -> {
//                    log.error("Payment failed for order {}: {}", orderId, e.getMessage());
//                    return Mono.error(new PaymentException("Payment service unavailable"));
//                });
//    }
}
