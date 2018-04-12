package com.keemsa.inventory.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;

/**
 * Created by sebastian on 11/07/16.
 */
public class InventoryDbHelper extends SQLiteOpenHelper {

    private static final String CREATE_PRODUCT_TABLE = "CREATE TABLE " +
            " " + InventoryContract.FeedInventory.TABLE_NAME + " (" +
            InventoryContract.FeedInventory._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            InventoryContract.FeedInventory.COLUMN_NAME_PRODUCT_NAME + " TEXT NOT NULL," +
            InventoryContract.FeedInventory.COLUMN_NAME_PRODUCT_QUANTITY + " INTEGER NOT NULL," +
            InventoryContract.FeedInventory.COLUMN_NAME_PRODUCT_PRICE + " REAL NOT NULL," +
            InventoryContract.FeedInventory.COLUMN_NAME_PRODUCT_PICTURE + " BLOB NOT NULL," +
            InventoryContract.FeedInventory.COLUMN_NAME_PRODUCT_SUPPLIER_NAME + " TEXT NOT NULL," +
            InventoryContract.FeedInventory.COLUMN_NAME_PRODUCT_SUPPLIER_EMAIL + " TEXT NOT NULL," +
            InventoryContract.FeedInventory.COLUMN_NAME_PRODUCT_SUPPLIER_PHONE + " INT NOT NULL" +
            ")";

    private static final String DELETE_PRODUCT_TABLE = "DROP TABLE IF EXISTS " + InventoryContract.FeedInventory.TABLE_NAME;

    public InventoryDbHelper(Context context){
        super(context, InventoryContract.DATABASE_NAME, null, InventoryContract.DATABASE_VERSION);
    }

    public void insertProductRecord(String name, int quantity, float price, Bitmap picture, String supplierName, String supplierEmail, int supplierPhone){

        // convert bitmap to blob
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        picture.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] bArray = baos.toByteArray();

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(InventoryContract.FeedInventory.COLUMN_NAME_PRODUCT_NAME, name);
        values.put(InventoryContract.FeedInventory.COLUMN_NAME_PRODUCT_QUANTITY, quantity);
        values.put(InventoryContract.FeedInventory.COLUMN_NAME_PRODUCT_PRICE, price);
        values.put(InventoryContract.FeedInventory.COLUMN_NAME_PRODUCT_PICTURE, bArray);
        values.put(InventoryContract.FeedInventory.COLUMN_NAME_PRODUCT_SUPPLIER_NAME, supplierName);
        values.put(InventoryContract.FeedInventory.COLUMN_NAME_PRODUCT_SUPPLIER_EMAIL, supplierEmail);
        values.put(InventoryContract.FeedInventory.COLUMN_NAME_PRODUCT_SUPPLIER_PHONE, supplierPhone);

        db.insert(InventoryContract.FeedInventory.TABLE_NAME, null, values);
    }

    public Cursor getAllProducts(){
        SQLiteDatabase db  = getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + InventoryContract.FeedInventory.TABLE_NAME, null);
    }

    public Cursor getProductById(int id){
        SQLiteDatabase db  = getReadableDatabase();

        String [] columns = {
                InventoryContract.FeedInventory._ID,
                InventoryContract.FeedInventory.COLUMN_NAME_PRODUCT_NAME,
                InventoryContract.FeedInventory.COLUMN_NAME_PRODUCT_QUANTITY,
                InventoryContract.FeedInventory.COLUMN_NAME_PRODUCT_PRICE,
                InventoryContract.FeedInventory.COLUMN_NAME_PRODUCT_PICTURE,
                InventoryContract.FeedInventory.COLUMN_NAME_PRODUCT_SUPPLIER_NAME,
                InventoryContract.FeedInventory.COLUMN_NAME_PRODUCT_SUPPLIER_EMAIL,
                InventoryContract.FeedInventory.COLUMN_NAME_PRODUCT_SUPPLIER_PHONE
        };

        String selection = InventoryContract.FeedInventory._ID + " = " + id;

        return db.query(
                InventoryContract.FeedInventory.TABLE_NAME,
                columns,
                selection,
                null,
                null,
                null,
                null
        );
    }

    public int updateProductQuantityById(int id, int quantity){
        SQLiteDatabase db = getReadableDatabase();

        ContentValues values = new ContentValues();
        values.put(InventoryContract.FeedInventory.COLUMN_NAME_PRODUCT_QUANTITY, quantity);

        String selection = InventoryContract.FeedInventory._ID + " = " + id;

        return db.update(
                InventoryContract.FeedInventory.TABLE_NAME,
                values,
                selection,
                null
        );
    }

    public int deleteProducById(int id){
        SQLiteDatabase db = getWritableDatabase();

        String selection = InventoryContract.FeedInventory._ID + " = " + id;

        return db.delete(
                InventoryContract.FeedInventory.TABLE_NAME,
                selection,
                null
        );
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_PRODUCT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(DELETE_PRODUCT_TABLE);
        onCreate(sqLiteDatabase);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onDowngrade(db, oldVersion, newVersion);
    }
}

