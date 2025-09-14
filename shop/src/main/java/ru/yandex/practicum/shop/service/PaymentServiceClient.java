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
}
