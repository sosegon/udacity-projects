package com.keemsa.inventory;

import com.keemsa.inventory.model.Product;

/**
 * Created by sebastian on 12/07/16.
 */
public interface ProductDetailsAsyncResponse {

    void processProductDetails(Product product);
}
