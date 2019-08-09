package com.udacity.course3.reviews.repository;

import com.udacity.course3.reviews.model.Product;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class ProductRepositoryCustomImpl implements ProductRepositoryCustom {
    @Autowired
    ProductRepository productRepository;

    @Override
    public List<Product> findProductsByName(String name) {
        Iterable<Product> itProduct = productRepository.findAll();
        List<Product> products = new ArrayList<Product>();

        itProduct.forEach(product -> {
            if(product.getName().toLowerCase().contains(name.toLowerCase())) {
                products.add(product);
            }
        });

        return products;
    }

    @Override
    public List<Product> findProductsCheaperThan(float price) {
        Iterable<Product> itProduct = productRepository.findAll();
        List<Product> products = new ArrayList<Product>();

        itProduct.forEach(product -> {
            if(product.getPrice() < price) {
                products.add(product);
            }
        });

        return products;
    }

    @Override
    public List<Product> findProductsMoreExpensiveThan(float price) {
        Iterable<Product> itProduct = productRepository.findAll();
        List<Product> products = new ArrayList<Product>();

        itProduct.forEach(product -> {
            if(product.getPrice() > price) {
                products.add(product);
            }
        });

        return products;
    }

    @Override
    public List<Product> findProductsBetweenPrices(float min, float max) {
        Iterable<Product> itProduct = productRepository.findAll();
        List<Product> products = new ArrayList<Product>();

        itProduct.forEach(product -> {
            if(product.getPrice() > min &&
                    product.getPrice() < max) {
                products.add(product);
            }
        });

        return products;
    }

    @Override
    public List<Product> findProductsByNameCheaperThan(String name, float price) {
        Iterable<Product> itProduct = productRepository.findAll();
        List<Product> products = new ArrayList<Product>();

        itProduct.forEach(product -> {
            if(product.getPrice() < price &&
                    product.getName().toLowerCase().contains(name.toLowerCase())) {
                products.add(product);
            }
        });

        return products;
    }

    @Override
    public List<Product> findProductsByNameMoreExpensiveThan(String name, float price) {
        Iterable<Product> itProduct = productRepository.findAll();
        List<Product> products = new ArrayList<Product>();

        itProduct.forEach(product -> {
            if(product.getPrice() > price &&
                    product.getName().toLowerCase().contains(name.toLowerCase())) {
                products.add(product);
            }
        });

        return products;
    }

    @Override
    public List<Product> findProductsByNameBetweenPrices(String name, float min, float max) {
        Iterable<Product> itProduct = productRepository.findAll();
        List<Product> products = new ArrayList<Product>();

        itProduct.forEach(product -> {
            if(product.getPrice() > min &&
                    product.getPrice() < max &&
                    product.getName().toLowerCase().contains(name.toLowerCase())) {
                products.add(product);
            }
        });

        return products;
    }

    @Override
    public List<Product> findProductByNameWithReviews(String name) {
        Iterable<Product> itProduct = productRepository.findAll();
        List<Product> products = new ArrayList<Product>();

        itProduct.forEach(product -> {
            if(product.getReviews().size() > 0 &&
                    product.getName().toLowerCase().contains(name.toLowerCase())) {
                products.add(product);
            }
        });

        return products;
    }
}
