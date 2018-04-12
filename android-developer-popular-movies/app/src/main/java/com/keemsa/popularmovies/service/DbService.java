package com.keemsa.popularmovies.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by sebastian on 11/1/16.
 */
public class DbService extends IntentService {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({DB_SERVICE_INSERT, DB_SERVICE_UPDATE, DB_SERVICE_QUERY, DB_SERVICE_DELETE, DB_SERVICE_BULK_INSERT})
    public @interface DbOperations{}

    public static final String DB_SERVICE_OPERATION = "dbso";
    public static final String DB_SERVICE_VALUE = "dbsv";
    public static final int DB_SERVICE_INSERT = 0;
    public static final int DB_SERVICE_UPDATE = 1;
    public static final int DB_SERVICE_QUERY = 2;
    public static final int DB_SERVICE_DELETE = 3;
    public static final int DB_SERVICE_BULK_INSERT = 4;

    private final static String LOG_TAG = DbService.class.getSimpleName();

    public DbService() {
        super("DbService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Uri movieUri = intent.getData();

        switch (intent.getIntExtra(DB_SERVICE_OPERATION, -1)) {
            case DB_SERVICE_UPDATE:
                ContentValues cv = intent.getParcelableExtra(DB_SERVICE_VALUE);
                if(cv != null){
                    getContentResolver().update(movieUri, cv, null, null);
                }
                break;
            /*
                TODO: add the rest of DB operations
             */
            default:
                break;
        }
    }
}
