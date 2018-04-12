package com.keemsa.inventory.task;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;

import com.keemsa.inventory.ProductUpdateQuantityAsyncResponse;
import com.keemsa.inventory.ProductViewHolder;
import com.keemsa.inventory.database.InventoryDbHelper;

/**
 * Created by sebastian on 14/07/16.
 */
public class UpdateQuantityProductTask extends AsyncTask<Context, Void, Integer> {

    private ProductUpdateQuantityAsyncResponse productUpdateQuantityAsyncResponse;
    private int productId;
    private int quantity;

    public UpdateQuantityProductTask(ProductUpdateQuantityAsyncResponse productUpdateQuantityAsyncResponse, int productId, int quantity) {
        this.productUpdateQuantityAsyncResponse = productUpdateQuantityAsyncResponse;
        this.productId = productId;
        this.quantity = quantity;
    }

    @Override
    protected Integer doInBackground(Context... contexts) {
        InventoryDbHelper dbHelper = new InventoryDbHelper(contexts[0]);
        return new Integer(dbHelper.updateProductQuantityById(this.productId, this.quantity));
    }

    @Override
    protected void onPostExecute(Integer integer) {
        if(integer.equals(1)) {
            productUpdateQuantityAsyncResponse.processQuantity(this.quantity);
        }
    }
}
