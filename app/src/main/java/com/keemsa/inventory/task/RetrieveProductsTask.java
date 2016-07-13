package com.keemsa.inventory.task;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;

import com.keemsa.inventory.ProductsAsyncResponse;
import com.keemsa.inventory.database.InventoryContract;
import com.keemsa.inventory.database.InventoryDbHelper;
import com.keemsa.inventory.model.Product;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sebastian on 12/07/16.
 */
public class RetrieveProductsTask extends AsyncTask<Context, Void, Cursor> {

    private ProductsAsyncResponse productsAsyncResponse;

    public RetrieveProductsTask(ProductsAsyncResponse productsAsyncResponse) {
        this.productsAsyncResponse = productsAsyncResponse;
    }

    @Override
    protected Cursor doInBackground(Context... contexts) {
        InventoryDbHelper dbHelper = new InventoryDbHelper(contexts[0]);
        return dbHelper.getAllProducts();
    }

    @Override
    protected void onPostExecute(Cursor cursor) {
        List<Product> products = processCursor(cursor);
        productsAsyncResponse.processProducts(products);
    }

    private List<Product> processCursor(Cursor cursor){
        List<Product> productList = new ArrayList<Product>();
        if(cursor.moveToFirst()){
            while(cursor.isAfterLast() == false){
                int productId = cursor.getInt(cursor.getColumnIndex(InventoryContract.FeedInventory._ID));
                String productName = cursor.getString(cursor.getColumnIndex(InventoryContract.FeedInventory.COLUMN_NAME_PRODUCT_NAME));
                int productQuantity = cursor.getInt(cursor.getColumnIndex(InventoryContract.FeedInventory.COLUMN_NAME_PRODUCT_QUANTITY));
                float productPrice = cursor.getFloat(cursor.getColumnIndex(InventoryContract.FeedInventory.COLUMN_NAME_PRODUCT_PRICE));

                Product currentProduct = new Product(productId, productName, productQuantity, productPrice);
                productList.add(currentProduct);
                cursor.moveToNext();
            }
        }

        return productList;
    }
}
