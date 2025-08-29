package ru.yandex.practicum.mvc_internet_shop.service;


import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.mvc_internet_shop.mapper.ProductMapper;
import ru.yandex.practicum.mvc_internet_shop.model.Product;
import ru.yandex.practicum.mvc_internet_shop.model.dto.FilterProductDTO;
import ru.yandex.practicum.mvc_internet_shop.model.dto.OrderDTO;
import ru.yandex.practicum.mvc_internet_shop.model.dto.ProductDTO;
import ru.yandex.practicum.mvc_internet_shop.model.exception.BadRequestException;
import ru.yandex.practicum.mvc_internet_shop.repository.ProductRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final OrderService orderService;

    /**
     * Получение полного списка товаров
     **/
    public List<ProductDTO> findAll() {
        OrderDTO order = orderService.getOrderInCart();
        List<ProductDTO> productsInOrder = order.getProducts();

        List<Product> products = productRepository.findAll();
        Map<Integer, Integer> mapCountProduct = getCountToProductInCart(productsInOrder);
        Map<Integer, Integer> mapItemIdProduct = getItemIdToProductInCart(productsInOrder);
        return productMapper.toDto(products, mapCountProduct, mapItemIdProduct);
    }

    /**
     * Получение списка продуктов c паджинацией
     * */
    public Page<ProductDTO> getProductsByFilter(FilterProductDTO filter) {
        // получаем/создаем заказ в статусе Create
        OrderDTO order = orderService.getOrderInCart();
        List<ProductDTO> productsInOrder = order.getProducts();

        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize());
        int pageSize = pageable.getPageSize();
        int pageNumber = pageable.getPageNumber();

        String searchSqlText = filter.getSearch().isEmpty() ? null : filter.getSearch();
        long totalPage =  productRepository.countTotalProduct(searchSqlText);
        List<Product> products = productRepository.findByFilter(pageSize, pageSize * pageNumber, searchSqlText, filter.getSort());
        //собираем маппинг с количеством продукта в корзине
        Map<Integer, Integer> mapCountProduct = getCountToProductInCart(productsInOrder);
        //собираем маппинг с идентификатором элемента в корзине
        Map<Integer, Integer> mapItemIdProduct = getItemIdToProductInCart(productsInOrder);
        //формируем dto
        List<ProductDTO> dto = productMapper.toDto(products, mapCountProduct, mapItemIdProduct);
        return new PageImpl<>(dto, pageable, totalPage);
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
    public ProductDTO getProductById(Integer id) {
        OrderDTO order = orderService.getOrderInCart();
        List<ProductDTO> productsInOrder = order.getProducts();

        Product product = productRepository.findById(id).orElseThrow(() -> new BadRequestException("Incorrect product id"));
        if (productsInOrder != null && !productsInOrder.isEmpty()) {
            productsInOrder = productsInOrder.stream().filter(it -> it.getId().equals(id)).toList();
        }
        if (productsInOrder != null && !productsInOrder.isEmpty()) {
            return productMapper.toDto(product, productsInOrder.get(0).getCount(), productsInOrder.get(0).getItemId());
        } else {
            return productMapper.toDto(product,null, null);
        }
    }


}
