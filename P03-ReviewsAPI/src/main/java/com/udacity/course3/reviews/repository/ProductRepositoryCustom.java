package com.udacity.course3.reviews.repository;

import com.udacity.course3.reviews.model.Product;

import java.util.List;

public interface ProductRepositoryCustom {
    List<Product> findProductsByName(String name);
    List<Product> findProductsCheaperThan(float price);
    List<Product> findProductsMoreExpensiveThan(float price);
    List<Product> findProductsBetweenPrices(float min, float max);
    List<Product> findProductsByNameCheaperThan(String name, float price);
    List<Product> findProductsByNameMoreExpensiveThan(String name, float price);
    List<Product> findProductsByNameBetweenPrices(String name, float min, float max);
    List<Product> findProductByNameWithReviews(String name);
}
