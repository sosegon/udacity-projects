package com.sam_chordas.android.stockhawk.service;

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.IntDef;
import android.support.v4.os.ResultReceiver;
import android.util.Log;

import com.sam_chordas.android.stockhawk.AppStatus;
import com.sam_chordas.android.stockhawk.R;
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
  @IntDef({CP_SERVICE_INSERT, CP_SERVICE_UPDATE, CP_SERVICE_QUERY, CP_SERVICE_DELETE, CP_SERVICE_BULK_INSERT, CP_SERVICE_UPDATE_TO_QUERY_SERVER})
  public @interface DbOperations{}

  public static final String CP_SERVICE_OPERATION = "dbso";
  public static final String CP_SERVICE_VALUE = "dbsv";
  public static final int CP_SERVICE_INSERT = 0;
  public static final int CP_SERVICE_UPDATE = 1;
  public static final int CP_SERVICE_QUERY = 2;
  public static final int CP_SERVICE_DELETE = 3;
  public static final int CP_SERVICE_BULK_INSERT = 4;
  public static final int CP_SERVICE_UPDATE_TO_QUERY_SERVER = 5;

  private final static String LOG_TAG = ContentProviderService.class.getSimpleName();

  public ContentProviderService() {
    super("ContentProviderService");
  }

  @Override
  protected void onHandleIntent(Intent intent) {

    switch (intent.getIntExtra(CP_SERVICE_OPERATION, -1)) {
      case CP_SERVICE_UPDATE_TO_QUERY_SERVER:
        Cursor c = getContentResolver().query(
                QuoteProvider.Quotes.CONTENT_URI,
                Projections.STOCK,
                null,
                null,
                null
        );

        ArrayList<ContentProviderOperation> batchOperations = Utils.updateStocksToQueryServerOperation(c);

        try {
          getContentResolver().applyBatch(
                  QuoteProvider.AUTHORITY,
                  batchOperations
          );

          ResultReceiver receiver = intent.getParcelableExtra("receiver");
          Bundle args = new Bundle();
          args.putString("cpOperation", "done");
          receiver.send(0, args);
        } catch (RemoteException | OperationApplicationException e) {
          saveAppStatus(AppStatus.STOCK_STATUS_DATABASE_ERROR);
          Log.d(LOG_TAG, "Error when a applying batch insert.", e);
        }

        break;
            /*
                TODO: add the rest of DB operations
             */
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
