package ru.yandex.practicum.shop.service;


import lombok.AllArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.shop.mapper.ProductMapper;
import ru.yandex.practicum.shop.model.Product;
import ru.yandex.practicum.shop.model.dto.FilterProductDTO;
import ru.yandex.practicum.shop.model.dto.OrderDTO;
import ru.yandex.practicum.shop.model.dto.ProductDTO;
import ru.yandex.practicum.shop.model.exception.BadRequestException;
import ru.yandex.practicum.shop.repository.ProductRepository;
import ru.yandex.practicum.shop.repository.ProductsInOrderRepository;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ProductsInOrderRepository productsInOrderRepository;
    private final CacheService cacheService;


    /**
     * Получение полного списка продуктов из кэша или БД
     */
    public Mono<List<Product>> getAllProducts() {
        return cacheService.getListProductFromCache()
                .switchIfEmpty(Mono.defer(() ->
                        productRepository.findAll()
                                .collectList()
                                .flatMap(products ->
                                        cacheService.saveListProductToCache(products)
                                                .then(Mono.just(products))
                                )
                ));
    }

    /**
     * Фильтрация и сортировка списка продуктов
     */
    public Mono<List<Product>> getSortedListProduct(FilterProductDTO filter) {
        String search = StringUtils.hasText(filter.getSearch()) ? filter.getSearch().toLowerCase() : "";
        String sort = StringUtils.hasText(filter.getSort()) ? filter.getSort() : null;
        return getAllProducts()
                .flatMap(products -> {
                    // Фильтрация продуктов
                    List<Product> filteredProducts = products.stream()
                            .filter(it -> it.getTitle().toLowerCase().contains(search) ||
                                    it.getDescription().toLowerCase().contains(search))
                            .collect(Collectors.toList());

                    // Определение компаратора для сортировки
                    Comparator<Product> comparator;
                    if (sort == null) {
                        comparator = Comparator.comparing(Product::getId);
                    } else if (sort.contains("title") && sort.contains("asc")) {
                        comparator = Comparator.comparing(Product::getTitle, String.CASE_INSENSITIVE_ORDER);
                    } else if (sort.contains("price") && sort.contains("asc")) {
                        comparator = Comparator.comparing(Product::getPrice);
                    } else if (sort.contains("title") && sort.contains("desc")) {
                        comparator = Comparator.comparing(Product::getTitle, String.CASE_INSENSITIVE_ORDER).reversed();
                    } else if (sort.contains("price") && sort.contains("desc")) {
                        comparator = Comparator.comparing(Product::getPrice).reversed();
                    } else {
                        comparator = Comparator.comparing(Product::getId);
                    }

                    // Сортировка и возврат результата
                    return Mono.just(filteredProducts.stream()
                            .sorted(comparator)
                            .collect(Collectors.toList()));
                });
    }

    /**
     * Получение списка продуктов c паджинацией
     * */
    public Mono<Page<ProductDTO>> getProductsByFilter(OrderDTO order, FilterProductDTO filter) {
        // получаем/создаем заказ в статусе Create
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize());
        return getSortedListProduct(filter)
                .map(products -> {
                    List<ProductDTO> productFromCart = order.getProducts();
                    int startIndex = pageable.getPageSize() * pageable.getPageNumber();
                    int finishIndex = (startIndex + pageable.getPageSize() >= products.size()) ? products.size()-1 : startIndex + pageable.getPageSize();
                    List<Product> pageProduct = products.subList(startIndex, finishIndex);

                    Map<Integer, Integer> countMap = getCountToProductInCart(productFromCart);
                    Map<Integer, Integer> itemIdMap = getItemIdToProductInCart(productFromCart);

                    List<ProductDTO> dtoList = productMapper.toDto(pageProduct, countMap, itemIdMap);
                    return new PageImpl<>(dtoList, pageable, products.size());
                });
    }

    /**
     * Получение маппинга (id товара, количество товара) относительно товаров заказа в корзине
     * */
    public Map<Integer, Integer> getCountToProductInCart(List<ProductDTO> productsInOrder) {
        Map<Integer, Integer> mapCountProduct = new HashMap<>();
        productsInOrder.forEach(it -> mapCountProduct.put(it.getId(), it.getCount()));
        return mapCountProduct;
    }

    /**
     * Получение маппинга (id товара, id товара в заказе)
     * */
    public Map<Integer, Integer> getItemIdToProductInCart(List<ProductDTO> productsInOrder) {
        Map<Integer, Integer> mapItemIdProduct = new HashMap<>();
        productsInOrder.forEach(it -> mapItemIdProduct.put(it.getId(), it.getItemId()));
        return mapItemIdProduct;
    }

    /**
     * Получение продукта по ID с кэшированием
     */
    public Mono<ProductDTO> getProductById(Integer orderId, Integer productId) {
        return cacheService.getProductByIdFromCache(productId)
                .switchIfEmpty(Mono.defer(() ->
                        productRepository.findById(productId)
                                .switchIfEmpty(Mono.error(new BadRequestException("Incorrect product id")))
                                .flatMap(cacheService::saveProductByIdToCache)
                ))
                .flatMap(product ->
                        productsInOrderRepository.findFirstByOrderIdAndProductId(orderId, product.getId())
                                       .map(item -> productMapper.toDto(product, item.getProductCount(), item.getId()))
                                       .defaultIfEmpty(productMapper.toDto(product, null, null))

                );
    }


}
