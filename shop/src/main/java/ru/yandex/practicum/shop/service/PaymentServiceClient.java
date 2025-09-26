package ru.yandex.practicum.shop.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import ru.yandex.practicum.ApiClient;
import ru.yandex.practicum.api.DefaultApi;

@Component
public class PaymentServiceClient extends DefaultApi {

    public PaymentServiceClient(@Qualifier("paymentServiceWebClient") WebClient webClient) {
        // Передаем WebClient напрямую в конструктор ApiClient
        super(new ApiClient(webClient));
    }
}
