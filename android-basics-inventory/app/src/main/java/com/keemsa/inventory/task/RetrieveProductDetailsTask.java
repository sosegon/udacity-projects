package com.keemsa.inventory.task;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.keemsa.inventory.ProductDetailsAsyncResponse;
import com.keemsa.inventory.database.InventoryContract;
import com.keemsa.inventory.database.InventoryDbHelper;
import com.keemsa.inventory.model.Product;

import java.sql.Blob;

/**
 * Created by sebastian on 12/07/16.
 */
public class RetrieveProductDetailsTask extends AsyncTask<Context, Void, Cursor> {

    private ProductDetailsAsyncResponse productDetailsAsyncResponse;
    private int productId;

    public RetrieveProductDetailsTask(ProductDetailsAsyncResponse productDetailsAsyncResponse, int productId) {
        this.productDetailsAsyncResponse = productDetailsAsyncResponse;
        this.productId = productId;
    }

    @Override
    protected Cursor doInBackground(Context... contexts) {
        InventoryDbHelper dbHelper = new InventoryDbHelper(contexts[0]);
        return dbHelper.getProductById(this.productId);
    }

    @Override
    protected void onPostExecute(Cursor cursor) {
        Product product = processCursor(cursor);

        productDetailsAsyncResponse.processProductDetails(product);
    }

    private Product processCursor(Cursor cursor){
        if(cursor.moveToFirst()){
            int productId = cursor.getInt(cursor.getColumnIndex(InventoryContract.FeedInventory._ID));
            String productName = cursor.getString(cursor.getColumnIndex(InventoryContract.FeedInventory.COLUMN_NAME_PRODUCT_NAME));
            int productQuantity = cursor.getInt(cursor.getColumnIndex(InventoryContract.FeedInventory.COLUMN_NAME_PRODUCT_QUANTITY));
            float productPrice = cursor.getFloat(cursor.getColumnIndex(InventoryContract.FeedInventory.COLUMN_NAME_PRODUCT_PRICE));
            String productSupplierName = cursor.getString(cursor.getColumnIndex(InventoryContract.FeedInventory.COLUMN_NAME_PRODUCT_SUPPLIER_NAME));
            String productSupplierEmail = cursor.getString(cursor.getColumnIndex(InventoryContract.FeedInventory.COLUMN_NAME_PRODUCT_SUPPLIER_EMAIL));
            int productSupplierPhone = cursor.getInt(cursor.getColumnIndex(InventoryContract.FeedInventory.COLUMN_NAME_PRODUCT_SUPPLIER_PHONE));
            byte[] blbProductPicture = cursor.getBlob(cursor.getColumnIndex(InventoryContract.FeedInventory.COLUMN_NAME_PRODUCT_PICTURE));
            Bitmap bmProductPicture = BitmapFactory.decodeByteArray(blbProductPicture, 0, blbProductPicture.length);

            Product product = new Product(
                    productId,
                    productName,
                    productQuantity,
                    productPrice,
                    bmProductPicture,
                    productSupplierName,
                    productSupplierEmail,
                    productSupplierPhone);

            cursor.close();

            return product;
        }
        return null;
    }
}

