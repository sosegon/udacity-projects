package com.keemsa.inventory;

import com.keemsa.inventory.model.Product;

import java.util.List;

/**
 * Created by sebastian on 12/07/16.
 */
public interface ProductsAsyncResponse {

    void processProducts(List<Product> products);
}
