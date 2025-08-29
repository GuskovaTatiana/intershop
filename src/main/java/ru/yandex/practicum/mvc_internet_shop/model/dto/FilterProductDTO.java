package ru.yandex.practicum.mvc_internet_shop.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class FilterProductDTO {
    private Integer page; // Номер страницы
    private Integer size; // Размер страницы
    private String search; // Строка поиска по Названию или описанию
    private String sort; // Сортировка


    public void copy(FilterProductDTO empty) {
        if (empty.getPage() != null) {
            this.page = empty.getPage();
        }
        if (empty.getSize() != null) {
            this.size = empty.getSize();
            this.page = 0;
        }
        if (empty.getSearch() != null) {
            this.search = empty.getSearch();
            this.page = 0;
        }
        if (empty.getSort() != null) {
            this.sort = empty.getSort();
        }
    }
}
