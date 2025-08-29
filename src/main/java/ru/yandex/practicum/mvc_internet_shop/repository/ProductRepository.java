package ru.yandex.practicum.mvc_internet_shop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.mvc_internet_shop.model.Product;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {


    @Query(nativeQuery = true,
            value = "select * from t_products\n" +
                    "where COALESCE(:search, '') = '' " +
                    "OR lower(title) like lower(CONCAT('%',:search,'%')) " +
                    "OR lower(description) like lower(CONCAT('%',:search,'%'))\n" +
                    "ORDER BY    CASE WHEN :sort = 'title asc' THEN title END ASC,\n" +
                    "            CASE WHEN :sort = 'title desc' THEN title END DESC,\n" +
                    "            CASE WHEN :sort = 'price asc' THEN price END ASC,\n" +
                    "            CASE WHEN :sort = 'price desc' THEN price END DESC,\n" +
                    "            id ASC" +
                    " LIMIT :size OFFSET :offset")
    List<Product> findByFilter(@Param(value = "size") int size,
                               @Param(value = "offset") int offset,
                               @Param(value = "search") String search,
                               @Param(value = "sort") String sort);

    @Query(nativeQuery = true,
            value = "select COUNT(*) from t_products where :search IS NULL \n" +
            " OR lower(title) like lower(CONCAT('%',:search,'%'))\n" +
            " OR lower(description) like lower(CONCAT('%',:search,'%'))")
    long countTotalProduct(@Param(value = "search") String search);
}
