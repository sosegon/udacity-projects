package com.keemsa.inventory.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.keemsa.inventory.ProductDetailsAsyncResponse;
import com.keemsa.inventory.R;
import com.keemsa.inventory.database.InventoryDbHelper;
import com.keemsa.inventory.model.Product;
import com.keemsa.inventory.task.RetrieveProductDetailsTask;

public class DetailedProductActivity extends AppCompatActivity implements ProductDetailsAsyncResponse{

    TextView txt_detailed_product_id,
            txt_detailed_product_name,
            txt_detailed_product_quantity,
            txt_detailed_product_price,
            txt_detailed_product_supplier_name,
            txt_detailed_product_supplier_email,
            txt_detailed_product_supplier_phone,
            txt_detailed_product_no_details;

    ImageView imv_detailed_product_picture;

    LinearLayout ll_detailed_product_overall;

    Button btn_detailed_product_increase,
            btn_detailed_product_decrease,
            btn_detailed_product_contact_supplier,
            btn_detailed_product_delete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_product);

        txt_detailed_product_id = (TextView) findViewById(R.id.txt_detailed_product_id);
        txt_detailed_product_name = (TextView) findViewById(R.id.txt_detailed_product_name);
        txt_detailed_product_quantity = (TextView) findViewById(R.id.txt_detailed_product_quantity);
        txt_detailed_product_price = (TextView) findViewById(R.id.txt_detailed_product_price);
        txt_detailed_product_supplier_name = (TextView) findViewById(R.id.txt_detailed_product_supplier_name);
        txt_detailed_product_supplier_email = (TextView) findViewById(R.id.txt_detailed_product_supplier_email);
        txt_detailed_product_supplier_phone = (TextView) findViewById(R.id.txt_detailed_product_supplier_phone);

        imv_detailed_product_picture = (ImageView) findViewById(R.id.imv_detailed_product_picture);

        Bundle extras = getIntent().getExtras();

        if(extras != null){
            int productId = extras.getInt("PRODUCT_ID");
            RetrieveProductDetailsTask task = new RetrieveProductDetailsTask(DetailedProductActivity.this, productId);
            task.execute(DetailedProductActivity.this);

            int color = extras.getInt("COLOR");
            txt_detailed_product_name.setBackgroundColor(color);
        }

        ll_detailed_product_overall = (LinearLayout) findViewById(R.id.ll_detailed_product_overall);
        ll_detailed_product_overall.setVisibility(View.GONE);
        txt_detailed_product_no_details = (TextView) findViewById(R.id.txt_detailed_product_no_details);
        txt_detailed_product_no_details.setVisibility(View.GONE);

        btn_detailed_product_increase = (Button) findViewById(R.id.btn_detailed_product_increase);
        btn_detailed_product_decrease = (Button) findViewById(R.id.btn_detailed_product_decrease);
        btn_detailed_product_contact_supplier = (Button) findViewById(R.id.btn_detailed_product_contact_supplier);
        btn_detailed_product_delete = (Button) findViewById(R.id.btn_detailed_product_delete);

        btn_detailed_product_increase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pid = Integer.parseInt(txt_detailed_product_id.getText().toString());
                InventoryDbHelper db = new InventoryDbHelper(DetailedProductActivity.this);
                db.updateProductQuantityById(pid, Integer.parseInt(txt_detailed_product_quantity.getText().toString().trim()) + 1);
                RetrieveProductDetailsTask task = new RetrieveProductDetailsTask(DetailedProductActivity.this, pid);
                task.execute(DetailedProductActivity.this);
            }
        });

        btn_detailed_product_decrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pid = Integer.parseInt(txt_detailed_product_id.getText().toString());
                InventoryDbHelper db = new InventoryDbHelper(DetailedProductActivity.this);
                db.updateProductQuantityById(pid, Integer.parseInt(txt_detailed_product_quantity.getText().toString().trim()) - 1);
                RetrieveProductDetailsTask task = new RetrieveProductDetailsTask(DetailedProductActivity.this, pid);
                task.execute(DetailedProductActivity.this);
            }
        });

        btn_detailed_product_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayDeletionConfirmDialog();
            }
        });

        btn_detailed_product_contact_supplier.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayContactDialog();
            }
        });

    }

    @Override
    public void processProductDetails(Product product){
        if(product != null){
            processExistingProductDetails(product);
        }
        else{
            processNonExistingProductDetails();
        }
    }

    private void processExistingProductDetails(Product product){
        ll_detailed_product_overall.setVisibility(View.VISIBLE);
        txt_detailed_product_no_details.setVisibility(View.GONE);

        txt_detailed_product_id.setText(String.valueOf(product.getId()));
        txt_detailed_product_name.setText(" " + product.getName());
        txt_detailed_product_quantity.setText(" " + String.valueOf(product.getQuantity()));
        txt_detailed_product_price.setText(" $" + String.valueOf(product.getPrice()));
        txt_detailed_product_supplier_name.setText(" " + product.getSupplierName());
        txt_detailed_product_supplier_email.setText(" " + product.getSupplierEmail());
        txt_detailed_product_supplier_phone.setText(" " + String.valueOf(product.getSupplierPhone()));
        imv_detailed_product_picture.setImageBitmap(product.getPicture());
    }

    private void processNonExistingProductDetails(){
        ll_detailed_product_overall.setVisibility(View.GONE);
        txt_detailed_product_no_details.setVisibility(View.VISIBLE);
    }

    private void displayDeletionConfirmDialog(){
        AlertDialog.Builder confirmDialog = new AlertDialog.Builder(DetailedProductActivity.this);
        confirmDialog.setTitle(getResources().getString(R.string.label_delete_product))
                    .setCancelable(true)
                    .setPositiveButton(getResources().getString(R.string.label_delete), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            int pid = Integer.parseInt(txt_detailed_product_id.getText().toString());
                            InventoryDbHelper dbHelper = new InventoryDbHelper(DetailedProductActivity.this);
                            dbHelper.deleteProducById(pid);

                            Intent intMain = new Intent(DetailedProductActivity.this, MainActivity.class);
                            startActivity(intMain);
                        }
                    })
                    .setNegativeButton(getResources().getString(R.string.label_cancel), null)
                    .show();
    }

    private void displayContactDialog(){
        AlertDialog.Builder contactDialog = new AlertDialog.Builder(DetailedProductActivity.this);
        contactDialog.setTitle(getResources().getString(R.string.label_contact_supplier))
                .setItems(R.array.opt_contact_supplier, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(i == 0){
                            callSupplier();
                        }
                        else if(i == 1){
                            emailSupplier();
                        }
                    }
                })
                .show();
    }

    private void callSupplier(){
        String phone = txt_detailed_product_supplier_phone.getText().toString();
        Intent intCall = new Intent(Intent.ACTION_CALL);
        intCall.setData(Uri.parse("tel:" + phone));
        startActivity(intCall);
    }

    private void emailSupplier(){
        String email = txt_detailed_product_supplier_email.getText().toString();
        String name = txt_detailed_product_name.getText().toString();
        Intent intEmail = new Intent(Intent.ACTION_SENDTO);
        intEmail.setData(Uri.parse("mailto:" + email));
        intEmail.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.msg_email_supplier_subject) + " " + name);
        intEmail.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.msg_email_supplier_body) + " " + name);
        intEmail.putExtra(Intent.EXTRA_EMAIL, email);
        startActivity(intEmail);
    }
}
