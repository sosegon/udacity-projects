package com.keemsa.inventory;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by sebastian on 11/07/16.
 */
public class ProductViewHolder extends RecyclerView.ViewHolder {

    protected TextView txt_product_name;
    protected TextView txt_product_quantity;
    protected TextView txt_product_price;
    protected LinearLayout ll_container;

    public ProductViewHolder(View itemView){
        super(itemView);
        this.txt_product_name = (TextView) itemView.findViewById(R.id.txt_product_name);
        this.txt_product_quantity = (TextView) itemView.findViewById(R.id.txt_product_quantity);
        this.txt_product_price = (TextView) itemView.findViewById(R.id.txt_product_price);
        this.ll_container = (LinearLayout) itemView.findViewById(R.id.ll_container);
    }
}
