package ru.yandex.practicum.shop.service;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.RedisServerCommands;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.shop.model.Product;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CacheService {

    private final ReactiveRedisTemplate<String, Product> productRedisTemplate;
    private final ReactiveRedisTemplate<String, List<Product>> productListRedisTemplate;
    private final ObjectMapper objectMapper;
    private static final Duration CACHE_TTL = Duration.ofMinutes(3);
    private static final String ALL_PRODUCTS_CACHE_KEY = "products:all";
    private static final String PRODUCT_BY_ID_CACHE_KEY = "product:";


    private String getProductCacheKey(Integer productId) {
        return PRODUCT_BY_ID_CACHE_KEY + productId;
    }

    /**
     * Сохранение списка продуктов в кэш
     */
    public Mono<List<Product>> saveListProductToCache(List<Product> products) {
        return productListRedisTemplate.opsForValue()
                .set(ALL_PRODUCTS_CACHE_KEY, products, CACHE_TTL)
                .thenReturn(products);
    }

    /**
     * Получение списка продуктов из кэша
     */
        public Mono<List<Product>> getListProductFromCache() {
        return productListRedisTemplate.opsForValue()
                .get(ALL_PRODUCTS_CACHE_KEY)
                .map(object -> {
                    if (object instanceof List) {
                        JavaType type = objectMapper.getTypeFactory()
                                .constructCollectionType(List.class, Product.class);
                        return objectMapper.convertValue(object, type);
                    }
                    return Collections.<Product>emptyList();
                });
    }

    public Mono<Boolean> clearCache() {
        return productListRedisTemplate.getConnectionFactory()
                .getReactiveConnection()
                .serverCommands()
                .flushDb(RedisServerCommands.FlushOption.ASYNC)
                .then(Mono.just(true));
    }


    /**
     * Сохранение продукта в кэш
     */
    public Mono<Product> saveProductByIdToCache(Product product) {
        return productRedisTemplate.opsForValue()
                .set(getProductCacheKey(product.getId()), product, CACHE_TTL)
                .thenReturn(product);
    }

    /**
     * Получение одного продукта из кэша
     */
    public Mono<Product> getProductByIdFromCache(Integer productId) {
        String key = getProductCacheKey(productId);
        return productRedisTemplate.opsForValue()
                .get(key)
//                .ofType(Product.class)
                .filter(Objects::nonNull);
    }


}
