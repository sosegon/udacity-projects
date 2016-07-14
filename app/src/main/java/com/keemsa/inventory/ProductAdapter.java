package com.keemsa.inventory;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.keemsa.inventory.activity.DetailedProductActivity;
import com.keemsa.inventory.database.InventoryDbHelper;
import com.keemsa.inventory.model.Product;
import com.keemsa.inventory.task.UpdateQuantityProductTask;

import java.util.List;

/**
 * Created by sebastian on 11/07/16.
 */
public class ProductAdapter extends RecyclerView.Adapter<ProductViewHolder> {

    private List<Product> productsDataset;
    private Context mContext;

    public ProductAdapter(Context mContext, List<Product> productsDataset){
        this.productsDataset = productsDataset;
        this.mContext = mContext;
    }

    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ProductViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false));
    }

    @Override
    public void onBindViewHolder(final ProductViewHolder holder, int position) {
        final Product productItem = productsDataset.get(position);
        final int color = position % 2 == 0 ?
                mContext.getResources().getColor(R.color.green300) :
                mContext.getResources().getColor(R.color.blue300);

        holder.txt_product_name.setText(productItem.getName());
        holder.txt_product_quantity.setText(" " + productItem.getQuantity());
        holder.txt_product_price.setText(" $" + productItem.getPrice());

        holder.rl_container.setBackgroundColor(color);
        holder.rl_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intDetailedProduct = new Intent(mContext, DetailedProductActivity.class);
                intDetailedProduct.putExtra("PRODUCT_ID", productItem.getId());
                intDetailedProduct.putExtra("COLOR", color);
                mContext.startActivity(intDetailedProduct);
            }
        });

        holder.setProduct(productItem);

        if(holder.getProduct().getQuantity() == 0){
            holder.btn_item_sell.setEnabled(false);
        }
        else{
            holder.btn_item_sell.setEnabled(true);
        }

        holder.btn_item_sell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdateQuantityProductTask task = new UpdateQuantityProductTask(holder, holder.getProduct().getId(), holder.getProduct().getQuantity() - 1);
                task.execute(mContext);
            }
        });
    }

    @Override
    public int getItemCount() {
        return (productsDataset != null ? productsDataset.size() : 0);
    }
}
