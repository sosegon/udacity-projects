package com.keemsa.inventory.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.keemsa.inventory.R;
import com.keemsa.inventory.database.InventoryDbHelper;

public class AddProductActivity extends AppCompatActivity {

    EditText etx_product_name,
            etx_product_quantity,
            etx_product_price,
            etx_product_supplier_name,
            etx_product_supplier_email,
            etx_product_supplier_phone;

    Button btn_add_picture,
            btn_insert_product;

    ImageView imv_product_picture;

    private static final int CAMERA_REQUEST = 2000;
    private static final int GAllERY_REQUEST = 2001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        final InventoryDbHelper dbHelper = new InventoryDbHelper(AddProductActivity.this);

        etx_product_name = (EditText) findViewById(R.id.etx_product_name);
        etx_product_quantity = (EditText) findViewById(R.id.etx_product_quantity);
        etx_product_price = (EditText) findViewById(R.id.etx_product_price);
        etx_product_supplier_name = (EditText) findViewById(R.id.etx_product_supplier_name);
        etx_product_supplier_email = (EditText) findViewById(R.id.etx_product_supplier_email);
        etx_product_supplier_phone = (EditText) findViewById(R.id.etx_product_supplier_phone);

        btn_add_picture = (Button) findViewById(R.id.btn_add_picture);

        btn_add_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayImageDialog();
            }
        });

        btn_insert_product = (Button) findViewById(R.id.btn_insert_product);

        btn_insert_product.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String validateInputs = validateInputs();
                if(validateInputs.equals(getResources().getString(R.string.validate_inputs_ok) )){
                    dbHelper.insertProductRecord(
                            etx_product_name.getText().toString(),
                            Integer.parseInt(etx_product_quantity.getText().toString()),
                            Float.parseFloat(etx_product_price.getText().toString()),
                            ((BitmapDrawable)imv_product_picture.getDrawable()).getBitmap(),
                            etx_product_supplier_name.getText().toString(),
                            etx_product_supplier_email.getText().toString(),
                            Integer.parseInt(etx_product_supplier_phone.getText().toString())
                    );
                    Intent output = new Intent();
                    setResult(MainActivity.PRODUCT_ADDED_OK, output);
                    finish();
                }
                else {
                    Toast.makeText(AddProductActivity.this, validateInputs, Toast.LENGTH_LONG).show();
                }
            }
        });

        imv_product_picture = (ImageView) findViewById(R.id.imv_product_picture);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == CAMERA_REQUEST && resultCode == RESULT_OK){
            Bitmap bmPicture = (Bitmap) data.getExtras().get("data");
            imv_product_picture.setImageBitmap(bmPicture);
        }
        else if(requestCode == GAllERY_REQUEST && resultCode == RESULT_OK){
            Uri imageUri = data.getData();
            String [] filePath = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(imageUri, filePath, null, null, null);
            cursor.moveToFirst();

            String imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));
            cursor.close();

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 8;

            Bitmap bmPicture = BitmapFactory.decodeFile(imagePath, options);
            imv_product_picture.setImageBitmap(bmPicture);
        }
    }

    private void displayImageDialog(){
        AlertDialog.Builder imageDialog = new AlertDialog.Builder(AddProductActivity.this);
        imageDialog.setTitle(getResources().getString(R.string.label_add_picture));
        imageDialog.setItems(R.array.opt_add_picture, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(i == 0){
                    startCamera();
                }
                else if (i == 1){
                    startGallery();
                }
            }
        });
        imageDialog.show();
    }

    private void startCamera(){
        Intent intPicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intPicture, CAMERA_REQUEST);
    }

    private void startGallery(){
        Intent intPicture = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intPicture, GAllERY_REQUEST);
    }

    protected String validateInputs(){
        if(!validateProductName()){
            return getResources().getString(R.string.validate_inputs_product_name);
        }
        else if(!validateProductQuantity()){
            return getResources().getString(R.string.validate_inputs_product_quantity);
        }
        else if(!validateProductPrice()){
            return getResources().getString(R.string.validate_inputs_product_price);
        }
        else if(!validateProductSupplierName()){
            return getResources().getString(R.string.validate_inputs_product_supplier_name);
        }
        else if(!validateProductSupplierEmail()){
            return getResources().getString(R.string.validate_inputs_product_supplier_email);
        }
        else if(!validateProductSupplierPhone()){
            return getResources().getString(R.string.validate_inputs_product_supplier_phone);
        }
        else if(!validateProductPicture()){
            return getResources().getString(R.string.validate_inputs_product_picture);
        }

        return getResources().getString(R.string.validate_inputs_ok);
    }

    private boolean validateProductName(){
        return !etx_product_name.getText().toString().equals("");
    }

    private boolean validateProductQuantity(){
        try{
            Integer.parseInt(etx_product_quantity.getText().toString());
        }
        catch (NumberFormatException e){
            return false;
        }
        return true;
    }

    private boolean validateProductPrice(){
        try {
            Float.parseFloat(etx_product_price.getText().toString());
        }
        catch (NumberFormatException e){
            return false;
        }
        return true;
    }

    private boolean validateProductSupplierName(){
        return !etx_product_supplier_name.getText().toString().equals("");
    }

    private boolean validateProductSupplierEmail(){
        return etx_product_supplier_email.getText().toString().contains("@");
    }

    private boolean validateProductSupplierPhone(){
        try{
            Integer.parseInt(etx_product_supplier_phone.getText().toString());
        }
        catch (NumberFormatException e){
            return false;
        }
        return true;
    }

    private boolean validateProductPicture(){
        return imv_product_picture.getDrawable() != null;
    }
}
