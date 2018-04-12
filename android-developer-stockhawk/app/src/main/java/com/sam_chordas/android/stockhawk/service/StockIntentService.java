package com.sam_chordas.android.stockhawk.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.TaskParams;
import com.sam_chordas.android.stockhawk.AppStatus;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;

/**
 * Created by sam_chordas on 10/1/15.
 */
public class StockIntentService extends IntentService {

  public final static String LOG_TAG = StockIntentService.class.getSimpleName();


  public StockIntentService() {
    super(StockIntentService.class.getName());
  }

  public StockIntentService(String name) {
    super(name);
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    Log.d(StockIntentService.class.getSimpleName(), "Stock Intent Service");
    StockTaskService stockTaskService = new StockTaskService(this);
    Bundle args = new Bundle();
    String tag = intent.getStringExtra(StockTaskService.TAG_TAG);
    String symbol = intent.getStringExtra(StockTaskService.SYMBOL_TAG);
    boolean isHistoric = tag.equals(StockTaskService.STS_HISTORIC);
    boolean isInit = tag.equals(StockTaskService.STS_INIT);
    boolean isAdd = tag.equals(StockTaskService.STS_ADD);
    boolean isPeriodic = tag.equals(StockTaskService.STS_PERIODIC);

    if(!Utils.isNetworkAvailable(this)){
      saveAppStatus(AppStatus.STOCK_STATUS_NO_CONNECTION);

      if(isInit || isPeriodic){
        Intent contentProviderIntent = new Intent(this, ContentProviderService.class);
        contentProviderIntent.putExtra(
                ContentProviderService.CP_SERVICE_OPERATION,
                ContentProviderService.CP_SERVICE_UPDATE_AFTER_QUERY_SERVER_FAILURE
        );
        startService(contentProviderIntent);
      } else if(isHistoric){
        Utils.setSharedPreference(this, getString(R.string.pref_key_querying_historic), 0, true);
        Intent contentProviderIntent = new Intent(this, ContentProviderService.class);
        contentProviderIntent.putExtra(
                ContentProviderService.CP_SERVICE_OPERATION,
                ContentProviderService.CP_SERVICE_DELETE_DUMB_HISTORIC
        );
        startService(contentProviderIntent);
      }
      return;
    }

    if (isAdd || isHistoric) {
      args.putString(StockTaskService.SYMBOL_TAG, symbol);
    }

    // Insert dumb record to restart the loader and update ui properly
    if(isHistoric){
      Intent contentProviderIntent = new Intent(this, ContentProviderService.class);
      contentProviderIntent.putExtra(
              ContentProviderService.CP_SERVICE_OPERATION,
              ContentProviderService.CP_SERVICE_INSERT_DUMB_HISTORIC
      );
      startService(contentProviderIntent);
    }
    // We can call OnRunTask from the intent service to force it to run immediately instead of
    // scheduling a task.
    int result = stockTaskService.onRunTask(new TaskParams(intent.getStringExtra(StockTaskService.TAG_TAG), args));
    if (result == GcmNetworkManager.RESULT_SUCCESS) {
      // Succesful result? Not quite likely, even though the operation
      // throws a successful result some things may have been went wrong
      // like incomplete data from server, which has been already handled
      // by the task service. So basically, don't set status to STOCK_STATUS_OK
    } else if (result != GcmNetworkManager.RESULT_SUCCESS && isAdd) {
      // Failure when querying dat for new record, delete it.
      getContentResolver().delete(
              QuoteProvider.Quotes.withSymbol(symbol),
              null,
              null
      );
    } else if (result != GcmNetworkManager.RESULT_SUCCESS && (isInit || isPeriodic)) {
      Intent contentProviderIntent = new Intent(this, ContentProviderService.class);
      contentProviderIntent.putExtra(
              ContentProviderService.CP_SERVICE_OPERATION,
              ContentProviderService.CP_SERVICE_UPDATE_AFTER_QUERY_SERVER_FAILURE
      );
      startService(contentProviderIntent);
    }
    // Delete dumb record to restart the loader and update ui properly
    if(isHistoric){
      Utils.setSharedPreference(this, getString(R.string.pref_key_querying_historic), 0, true);
      Intent contentProviderIntent = new Intent(this, ContentProviderService.class);
      contentProviderIntent.putExtra(
              ContentProviderService.CP_SERVICE_OPERATION,
              ContentProviderService.CP_SERVICE_DELETE_DUMB_HISTORIC
      );
      startService(contentProviderIntent);
    }
  }

  private void saveAppStatus(@AppStatus.StockStatus int status) {
    Utils.setSharedPreference(
            this,
            getString(R.string.pref_key_stock_status),
            status,
            true
    );
  }
}
