package com.sam_chordas.android.stockhawk.service;

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.IntDef;
import android.support.v4.os.ResultReceiver;
import android.util.Log;

import com.sam_chordas.android.stockhawk.AppStatus;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.PriceColumns;
import com.sam_chordas.android.stockhawk.data.Projections;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

/**
 * Created by sebastian on 12/3/16.
 */
public class ContentProviderService extends IntentService {
  @Retention(RetentionPolicy.SOURCE)
  @IntDef({CP_SERVICE_UPDATE_TO_QUERY_SERVER, CP_SERVICE_UPDATE_AFTER_QUERY_SERVER_FAILURE})
  public @interface DbOperations{}

  public static final String CP_SERVICE_OPERATION = "cpso";
  public static final int CP_SERVICE_UPDATE_TO_QUERY_SERVER = 0;
  public static final int CP_SERVICE_UPDATE_AFTER_QUERY_SERVER_FAILURE = 1;
  public static final int CP_SERVICE_INSERT_DUMB_HISTORIC = 2;
  public static final int CP_SERVICE_DELETE_DUMB_HISTORIC = 3;

  private final String DUMB_HISTORIC_STOCK_NAME = "stock_dumb_historic";

  private final static String LOG_TAG = ContentProviderService.class.getSimpleName();

  public ContentProviderService() {
    super("ContentProviderService");
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    Cursor c;
    ArrayList<ContentProviderOperation> batchOperations;
    switch (intent.getIntExtra(CP_SERVICE_OPERATION, -1)) {
      case CP_SERVICE_UPDATE_TO_QUERY_SERVER:
        c = getContentResolver().query(
                QuoteProvider.Quotes.CONTENT_URI,
                Projections.STOCK,
                null,
                null,
                null
        );

        batchOperations = Utils.updateStocksUIPurpose(c, 1, 0);

        try {
          getContentResolver().applyBatch(
                  QuoteProvider.AUTHORITY,
                  batchOperations
          );

          // connect to the server and query new data
          Intent service = new Intent(this, StockIntentService.class);
          service.putExtra("tag", "init");
          startService(service);
        } catch (RemoteException | OperationApplicationException e) {
          saveAppStatus(AppStatus.STOCK_STATUS_DATABASE_ERROR);
          Log.d(LOG_TAG, "Error when a applying batch insert.", e);
        }
        break;

      case CP_SERVICE_UPDATE_AFTER_QUERY_SERVER_FAILURE:
        c = getContentResolver().query(
                QuoteProvider.Quotes.CONTENT_URI,
                Projections.STOCK,
                null,
                null,
                null
        );

        batchOperations = Utils.updateStocksUIPurpose(c, 0, 0);

        try {
          getContentResolver().applyBatch(
                  QuoteProvider.AUTHORITY,
                  batchOperations
          );
        } catch (RemoteException | OperationApplicationException e) {
          saveAppStatus(AppStatus.STOCK_STATUS_DATABASE_ERROR);
          Log.d(LOG_TAG, "Error when a applying batch insert.", e);
        }
        break;

      case CP_SERVICE_INSERT_DUMB_HISTORIC:
        ContentValues dumb = new ContentValues();
        dumb.put(PriceColumns.DATE, 0);
        dumb.put(PriceColumns.PRICE, 0);
        dumb.put(PriceColumns.STOCK_SYMBOL, DUMB_HISTORIC_STOCK_NAME);

        Uri u = getContentResolver().insert(
                QuoteProvider.Prices.CONTENT_URI,
                dumb
        );

        Log.e("uri", u.toString());
        break;

      case CP_SERVICE_DELETE_DUMB_HISTORIC:
        getContentResolver().delete(
                QuoteProvider.Prices.CONTENT_URI,
                PriceColumns.STOCK_SYMBOL + " = ?",
                new String[]{DUMB_HISTORIC_STOCK_NAME}
        );
        break;

      default:
        break;
    }
  }

  private void saveAppStatus(@AppStatus.StockStatus int status) {
    Utils.setSharedPreference(
            this,
            getString(R.string.pref_key_stock_status),
            status,
            false
    );
  }
}
