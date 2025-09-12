package ru.yandex.practicum.payment_service.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.api.PaymentsApi;
import ru.yandex.practicum.model.AmountRequest;
import ru.yandex.practicum.model.BalanceResponse;
import ru.yandex.practicum.model.PaymentBody;
import ru.yandex.practicum.model.PaymentResponse;

import ru.yandex.practicum.payment_service.model.exception.BadRequestException;
import ru.yandex.practicum.payment_service.service.PaymentService;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class PaymentController implements PaymentsApi {

    private final PaymentService service;

    @Override
    public Mono<ResponseEntity<BalanceResponse>> getBalance(ServerWebExchange exchange) {
        return service.getBalance()
                .map(balance -> new BalanceResponse()
                        .balance(balance))
                .map(ResponseEntity::ok)
                .onErrorResume(Exception.class,
                        e -> Mono.just(ResponseEntity.internalServerError().build()));
    }

    @Override
    public Mono<ResponseEntity<PaymentResponse>> processPayment(Mono<PaymentBody> paymentBody, ServerWebExchange exchange) {
        return paymentBody.flatMap(request ->
                service.processPayment(
                                request.getAmount()
                        )
                        .map(transaction -> new PaymentResponse()
                                .transactionId(transaction.getTransactionId())
                                .success(transaction.getSuccess())
                                .message(transaction.getMessage())
                        )
                        .map(ResponseEntity::ok)
                        .onErrorResume(IllegalArgumentException.class, e ->
                                Mono.just(ResponseEntity.badRequest()
                                        .header("X-Error-Code", "BAD_REQUEST")
                                        .header("X-Error-Message", e.getMessage())
                                        .build()))
                        .onErrorResume(Exception.class, e ->
                                Mono.just(ResponseEntity.internalServerError().build())));
    }

    @Override
    public Mono<ResponseEntity<Void>> setBalance(Mono<AmountRequest> amountRequest, ServerWebExchange exchange) {
        return amountRequest
                .flatMap(request -> {
                    if (request.getDepositAmount() == null) {
                        return Mono.just(ResponseEntity.badRequest().<Void>build());
                    }
                    return service.setBalance(request.getDepositAmount())
                        .then(Mono.just(ResponseEntity.created(null).<Void>build()));
                 })
                .onErrorResume(IllegalArgumentException.class, e ->
                        Mono.just(ResponseEntity.badRequest()
                                .header("X-Error-Code", "BAD_REQUEST")
                                .header("X-Error-Message", e.getMessage())
                                        .build()))
                .onErrorResume(BadRequestException.class, e ->
                        Mono.just(ResponseEntity.badRequest()
                                .header("X-Error-Code", "BALANCE_LIMIT_EXCEEDED")
                                .header("X-Error-Message", e.getMessage())
                                .build()))
                .onErrorResume(Exception.class, e ->
                        Mono.just(ResponseEntity.internalServerError().build()));
    }
}