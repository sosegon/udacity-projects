package com.keemsa.inventory.database;

import android.provider.BaseColumns;
import android.provider.SyncStateContract;

/**
 * Created by sebastian on 11/07/16.
 */
public class InventoryContract {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Inventory.db";

    public InventoryContract(){

    }

    public static abstract class FeedInventory implements BaseColumns{
        public static final String TABLE_NAME = "product";
        public static final String COLUMN_NAME_PRODUCT_NAME = "product_name";
        public static final String COLUMN_NAME_PRODUCT_QUANTITY = "product_quantity";
        public static final String COLUMN_NAME_PRODUCT_PRICE = "product_price";
        public static final String COLUMN_NAME_PRODUCT_PICTURE = "product_picture";
        public static final String COLUMN_NAME_PRODUCT_SUPPLIER_NAME = "product_supplier_name";
        public static final String COLUMN_NAME_PRODUCT_SUPPLIER_EMAIL = "product_supplier_email";
        public static final String COLUMN_NAME_PRODUCT_SUPPLIER_PHONE = "product_supplier_phone";
    }
}
