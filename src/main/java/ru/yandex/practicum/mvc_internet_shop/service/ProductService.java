package ru.yandex.practicum.mvc_internet_shop.service;


import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mvc_internet_shop.mapper.ProductMapper;
import ru.yandex.practicum.mvc_internet_shop.model.Product;
import ru.yandex.practicum.mvc_internet_shop.model.dto.FilterProductDTO;
import ru.yandex.practicum.mvc_internet_shop.model.dto.OrderDTO;
import ru.yandex.practicum.mvc_internet_shop.model.dto.ProductDTO;
import ru.yandex.practicum.mvc_internet_shop.model.exception.BadRequestException;
import ru.yandex.practicum.mvc_internet_shop.repository.ProductRepository;
import ru.yandex.practicum.mvc_internet_shop.repository.ProductsInOrderRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductsInOrderRepository productsInOrderRepository;
    private final ProductMapper productMapper;
    private final OrderService orderService;


    /**
     * Получение списка продуктов c паджинацией
     * */
    public Mono<Page<ProductDTO>> getProductsByFilter(FilterProductDTO filter) {
        // получаем/создаем заказ в статусе Create
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize());
        String searchSqlText = StringUtils.hasText(filter.getSearch()) ? filter.getSearch() : null;
        String sort = StringUtils.hasText(filter.getSort()) ? filter.getSort() : null;

        return orderService.getOrderInCart()
                .zipWith(productRepository.countTotalProduct(searchSqlText))
                .zipWith(productRepository.findByFilter(
                        pageable.getPageSize(),
                        pageable.getPageSize() * pageable.getPageNumber(),
                        searchSqlText,
                        sort
                ).collectList())
                .map(tuple -> {
                    OrderDTO order = tuple.getT1().getT1();
                    Long totalCount = tuple.getT1().getT2();
                    List<Product> products = tuple.getT2();

                    Map<Integer, Integer> countMap = getCountToProductInCart(order.getProducts());
                    Map<Integer, Integer> itemIdMap = getItemIdToProductInCart(order.getProducts());

                    List<ProductDTO> dtoList = productMapper.toDto(products, countMap, itemIdMap);
                    return new PageImpl<>(dtoList, pageable, totalCount);
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
     * Получение продукта по идентификатору
     * */
    public Mono<ProductDTO> getProductById(Integer id) {
        return productRepository.findById(id)
                .switchIfEmpty(Mono.error(new BadRequestException("Incorrect product id")))
                .flatMap(product ->
                        orderService.getOrderInCart()
                                .flatMap(order ->
                                        orderService.findByOrderIdAndProductId(order.getId(), product.getId())
                                                .map(item -> productMapper.toDto(product, item.getProductCount(), item.getId()))
                                )
                                .defaultIfEmpty(productMapper.toDto(product, null, null)) // Если продукта нет в корзине
                );
        }

}
