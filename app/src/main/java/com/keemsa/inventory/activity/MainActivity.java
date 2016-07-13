package com.keemsa.inventory.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.keemsa.inventory.ProductAdapter;
import com.keemsa.inventory.ProductsAsyncResponse;
import com.keemsa.inventory.R;
import com.keemsa.inventory.model.Product;
import com.keemsa.inventory.task.RetrieveProductsTask;

import java.util.List;

public class MainActivity extends AppCompatActivity implements ProductsAsyncResponse {

    Button btn_add_product;
    RecyclerView rcv_products;
    ProductAdapter adapter;
    TextView txt_message;
    ProgressBar pgr_load;

    public static final int PRODUCT_ADD_REQUEST = 1984;
    public static final int PRODUCT_ADDED_OK = 1985;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_add_product = (Button) findViewById(R.id.btn_add_product);

        btn_add_product.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intAddProduct = new Intent(MainActivity.this, AddProductActivity.class);
                startActivityForResult(intAddProduct, PRODUCT_ADD_REQUEST);
            }
        });

        rcv_products = (RecyclerView) findViewById(R.id.rcv_products);
        rcv_products.setLayoutManager(new LinearLayoutManager(this));

        txt_message = (TextView) findViewById(R.id.txt_message);

        pgr_load = (ProgressBar) findViewById(R.id.pgr_load) ;


        RetrieveProductsTask task = new RetrieveProductsTask(MainActivity.this);
        task.execute(MainActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == PRODUCT_ADD_REQUEST && resultCode == PRODUCT_ADDED_OK){
            Toast.makeText(MainActivity.this, getResources().getString(R.string.msg_new_product_added), Toast.LENGTH_LONG).show();
            RetrieveProductsTask task = new RetrieveProductsTask(MainActivity.this);
            task.execute(MainActivity.this);
        }
    }

    @Override
    public void processProducts(List<Product> products) {
        if(products == null || products.size() == 0){
            txt_message.setVisibility(View.VISIBLE);
        }
        else {
            txt_message.setVisibility(View.GONE);
        }
        adapter = new ProductAdapter(MainActivity.this, products);
        rcv_products.setAdapter(adapter);
        pgr_load.setVisibility(View.GONE);
    }
}
