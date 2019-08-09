package com.udacity.course3.reviews.repository;

import com.udacity.course3.reviews.model.Product;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.util.List;

@NoRepositoryBean
public interface ProductRepositoryCustom {
    @Query(
            value = "SELECT * FROM products p " +
                    "WHERE LOWER(p.name) LIKE CONCAT('%', LOWER(:name), '%')",
            nativeQuery = true)
    List<Product> findProductsByName(@Param("name") String name);

    @Query(
            value = "SELECT * FROM products p " +
                    "WHERE p.price < :price",
            nativeQuery = true)
    List<Product> findProductsCheaperThan(@Param("price") float price);

    @Query(
            value = "SELECT * FROM products p " +
                    "WHERE p.price > :price",
            nativeQuery = true)
    List<Product> findProductsMoreExpensiveThan(@Param("price") float price);

    @Query(
            value = "SELECT * FROM products p " +
                    "WHERE p.price > :min AND p.price < :max",
            nativeQuery = true)
    List<Product> findProductsBetweenPrices(@Param("min") float min, @Param("max") float max);

    @Query(
            value = "SELECT * FROM products p " +
                    "WHERE p.price < :price AND LOWER(p.name) LIKE CONCAT('%', LOWER(:name), '%')",
            nativeQuery = true)
    List<Product> findProductsByNameCheaperThan(@Param("name") String name, @Param("price") float price);

    @Query(
            value = "SELECT * FROM products p " +
                    "WHERE p.price > :price AND LOWER(p.name) LIKE CONCAT('%', LOWER(:name), '%')",
            nativeQuery = true)
    List<Product> findProductsByNameMoreExpensiveThan(@Param("name") String name, @Param("price") float price);

    @Query(
            value = "SELECT * FROM products p " +
                    "WHERE p.price >= :min AND p.price <= :max AND LOWER(p.name) LIKE CONCAT('%', LOWER(:name), '%')",
            nativeQuery = true)
    List<Product> findProductsByNameBetweenPrices(@Param("name") String name, @Param("min") float min, @Param("max") float max);

    @Query(
            value = "SELECT DISTINCT p.product_id, p.name, p.description, p.price " +
                    "FROM products p " +
                    "INNER JOIN reviews r ON p.product_id = r.product_id " +
                    "WHERE LOWER(p.name) LIKE CONCAT('%', LOWER(:name), '%')",
            nativeQuery = true)
    List<Product> findProductByNameWithReviews(@Param("name") String name);
}
