package ru.yandex.practicum.shop.service;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.shop.model.Product;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CacheService {

    private final ReactiveRedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private static final Duration CACHE_TTL = Duration.ofMinutes(3);
    private static final String ALL_PRODUCTS_CACHE_KEY = "products:all";

    private static final String ORDER_PRODUCTS_CACHE_KEY = "order:products:";
    private static final String PRODUCT_BY_ID_CACHE_KEY = "product:";


    private String getProductCacheKey(Integer productId) {
        return PRODUCT_BY_ID_CACHE_KEY + productId;
    }

    /**
     * Сохранение списка продуктов в кэш
     */
    public Mono<List<Product>> saveListProductToCache(List<Product> products) {
        return redisTemplate.opsForValue()
                .set(ALL_PRODUCTS_CACHE_KEY, products, CACHE_TTL)
                .thenReturn(products);
    }

    /**
     * Получение списка продуктов из кэша
     */
    public Mono<List<Product>> getListProductFromCache() {
        return redisTemplate.opsForValue()
                .get(ALL_PRODUCTS_CACHE_KEY)
                .map(object -> {
                    if (object instanceof List) {
//                        return Mono.just(object);
                        JavaType type = objectMapper.getTypeFactory()
                                .constructCollectionType(List.class, Product.class);
                        return objectMapper.convertValue(object, type);
                    }
                    return Collections.<Product>emptyList();
                });
    }
//
//    private List<Product> convertToProductList(Object obj) {
//        if (obj instanceof List) {
//            List<?> list = (List<?>) obj;
//            return list.stream()
//                    .map(item -> {
//                        if (item instanceof Product) {
//                            return (Product) item;
////                        } else if (item instanceof ) {
//                            // Преобразование LinkedHashMap в Product
//                            return objectMapper.convertValue(item, Product.class);
//                        }
//                        throw new IllegalArgumentException("Unsupported type: " + item.getClass());
//                    })
//                    .collect(Collectors.toList());
//        }
//        return Collections.emptyList();
//    }

    /**
     * Сохранение продукта в кэш
     */
    public Mono<Product> saveProductByIdToCache(Product product) {
        return redisTemplate.opsForValue()
                .set(getProductCacheKey(product.getId()), product, CACHE_TTL)
                .thenReturn(product);
    }


    /**
     * Получение продукта из кэша
     */
    public Mono<Product> getProductByIdFromCache(Integer productId) {
        return redisTemplate.opsForValue()
                .get(getProductCacheKey(productId))
                .map(object -> {
                    if (object instanceof Product) {
                        return (Product) object;
                    } else {
                        // Преобразование LinkedHashMap в Product
                        return objectMapper.convertValue(object, Product.class);
                    }
                })
                .onErrorResume(e -> {
                    return Mono.empty();
                });
    }


}
