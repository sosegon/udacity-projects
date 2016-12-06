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
    String tag = intent.getStringExtra("tag");
    String symbol = intent.getStringExtra("symbol");
    if (tag.equals("add") || tag.equals("historic")) {
      args.putString("symbol", symbol);
    }
    // We can call OnRunTask from the intent service to force it to run immediately instead of
    // scheduling a task.
    int result = stockTaskService.onRunTask(new TaskParams(intent.getStringExtra("tag"), args));
    if (result == GcmNetworkManager.RESULT_SUCCESS) {
      Utils.setSharedPreference(
              this,
              this.getString(R.string.pref_key_stock_status),
              AppStatus.STOCK_STATUS_OK,
              false
      );
    } else if (result != GcmNetworkManager.RESULT_SUCCESS && tag.equals("add")) {
      // Failure when querying dat for new record, delete it.
      getContentResolver().delete(
              QuoteProvider.Quotes.withSymbol(symbol),
              null,
              null
      );
    } else if (result != GcmNetworkManager.RESULT_SUCCESS && (tag.equals("init") || tag.equals("periodic"))) {
      Intent contentProviderIntent = new Intent(this, ContentProviderService.class);
      contentProviderIntent.putExtra(
              ContentProviderService.CP_SERVICE_OPERATION,
              ContentProviderService.CP_SERVICE_UPDATE_AFTER_QUERY_SERVER_FAILURE
      );
      startService(contentProviderIntent);
    }
  }
}
