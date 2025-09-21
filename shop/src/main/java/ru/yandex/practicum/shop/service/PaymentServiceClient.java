package ru.yandex.practicum.shop.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.ApiClient;
import ru.yandex.practicum.api.DefaultApi;

@Component
@RequiredArgsConstructor
public class PaymentServiceClient extends DefaultApi {
//    public PaymentServiceClient(@Value("${payment.service.url:http://localhost:8009/api}") String baseUrl) {
//        super(createApiClient(baseUrl));
//    }
//
//    private static ApiClient createApiClient(String baseUrl) {
//        ApiClient apiClient = new ApiClient();
//        apiClient.setBasePath(baseUrl);
//        return apiClient;
//    }
}
