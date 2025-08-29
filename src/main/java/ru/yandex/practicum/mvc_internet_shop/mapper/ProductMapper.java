package ru.yandex.practicum.mvc_internet_shop.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.mvc_internet_shop.model.Product;
import ru.yandex.practicum.mvc_internet_shop.model.dto.ProductDTO;

import java.util.List;
import java.util.Map;

@Component
public class ProductMapper {

    public ProductDTO toDto(Product empty, Integer count, Integer itemId) {
        return ProductDTO.builder()
                .id(empty.getId())
                .title(empty.getTitle())
                .imageUrl(empty.getImageUrl())
                .description(empty.getDescription())
                .price(empty.getPrice())
                .count(count)
                .itemId(itemId)
                .build();
    }
    public List<ProductDTO> toDto(List<Product> products, Map<Integer, Integer> countProduct, Map<Integer, Integer> orderItemIdToProduct) {
        return products.stream().map(it -> toDto(it, countProduct.get(it.getId()), orderItemIdToProduct.get(it.getId()))).toList();
    }
}
