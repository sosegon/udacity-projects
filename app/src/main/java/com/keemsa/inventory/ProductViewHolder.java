package com.keemsa.inventory;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.keemsa.inventory.model.Product;

/**
 * Created by sebastian on 11/07/16.
 */
public class ProductViewHolder extends RecyclerView.ViewHolder implements ProductUpdateQuantityAsyncResponse {

    protected TextView txt_product_name;
    protected TextView txt_product_quantity;
    protected TextView txt_product_price;
    protected RelativeLayout rl_container;
    protected Button btn_item_sell;
    protected Product product;

    public ProductViewHolder(View itemView){
        super(itemView);
        this.txt_product_name = (TextView) itemView.findViewById(R.id.txt_product_name);
        this.txt_product_quantity = (TextView) itemView.findViewById(R.id.txt_product_quantity);
        this.txt_product_price = (TextView) itemView.findViewById(R.id.txt_product_price);
        this.rl_container = (RelativeLayout) itemView.findViewById(R.id.rl_container);
        this.btn_item_sell = (Button) itemView.findViewById(R.id.btn_item_sell);
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    @Override
    public void processQuantity(int quantity) {
        txt_product_quantity.setText(String.valueOf(quantity));
        this.product.setQuantity(quantity);

        if(quantity == 0){
            btn_item_sell.setEnabled(false);
        }
        else{
            btn_item_sell.setEnabled(true);
        }
    }
}
