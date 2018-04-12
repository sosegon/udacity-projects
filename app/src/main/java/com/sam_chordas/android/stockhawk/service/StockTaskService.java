package com.sam_chordas.android.stockhawk.service;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.RemoteException;
import android.util.Log;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.sam_chordas.android.stockhawk.AppStatus;
import com.sam_chordas.android.stockhawk.NonExistingStockException;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.Projections;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by sam_chordas on 9/30/15.
 * The GCMTask service is primarily for periodic tasks. However, OnRunTask can be called directly
 * and is used for the initialization and adding task as well.
 */
public class StockTaskService extends GcmTaskService {

  public static final String STS_ADD = "add";
  public static final String STS_INIT = "init";
  public static final String STS_PERIODIC = "periodic";
  public static final String STS_HISTORIC = "historic";

  public final static String SYMBOL_TAG = "symbol";
  public final static String TAG_TAG = "tag";

  public static final String ACTION_DATA_UPDATED = "com.sam_chordas.android.stockhawk.ACTION_DATA_UPDATED";

  private String LOG_TAG = StockTaskService.class.getSimpleName();

  private Context mContext;
  private StringBuilder mStoredSymbols = new StringBuilder();

  /*
      Used to update a record in the db
   */
  private boolean isHistoric, isInit, isAdd, isPeriodic;

  public StockTaskService() {
  }

  public StockTaskService(Context context) {
    mContext = context;
  }

  private String fetchData(String url) {
    /*
        OkHttpClient was too troublesome
     */
    HttpURLConnection connection = null;
    BufferedReader reader = null;
    String stocksJson = null;
    InputStream stream = null;

    try {
      URL stockUrl = new URL(url);
      connection = (HttpURLConnection) stockUrl.openConnection();
      connection.setRequestMethod("GET");
      connection.setConnectTimeout(10000); // ten seconds to connect
      connection.setReadTimeout(10000); // see http://stackoverflow.com/a/24554486/1065981
      connection.connect();
      stream = connection.getInputStream();
    } catch (IOException e) {
      saveAppStatus(AppStatus.STOCK_STATUS_NO_RESPONSE);
      Log.d(LOG_TAG, "Error connecting to the server to query for stocks");
    }

    try {
      if (stream != null) {
        reader = new BufferedReader(new InputStreamReader(stream));
        String line;
        StringBuilder output = new StringBuilder();
        while ((line = reader.readLine()) != null) {
          output.append(line);
        }
        if (output.length() != 0) {
          stocksJson = output.toString();
        }
      }
    } catch (IOException e) {
      saveAppStatus(AppStatus.STOCK_STATUS_INVALID_DATA);
      Log.d(LOG_TAG, "Error reading the stream from the server.");
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
      if (reader != null) {
        try {
          reader.close();
        } catch (Exception e) {
          Log.d(LOG_TAG, "Error closing the stream.");
        }
      }
    }

    return stocksJson;
  }

  @Override
  public int onRunTask(TaskParams params) {
    if (mContext == null) {
      mContext = this;
    }

    StringBuilder urlStringBuilder = new StringBuilder();
    urlStringBuilder.append(mContext.getString(R.string.query_base_url)); // Base URL for the Yahoo query
    isHistoric = params.getTag().equals(STS_HISTORIC);
    isInit = params.getTag().equals(StockTaskService.STS_INIT);
    isAdd = params.getTag().equals(StockTaskService.STS_ADD);
    isPeriodic = params.getTag().equals(StockTaskService.STS_PERIODIC);
    if (isHistoric) {
      try {
        String currentDate = Utils.getCurrentDate(),
                previousMonthDate = Utils.getPreviousMonthDate(currentDate),
                symbol = params.getExtras().getString(StockTaskService.SYMBOL_TAG),
                queryString = mContext.getString(
                        R.string.query_statement_historic,
                        symbol,
                        previousMonthDate,
                        currentDate
                );

        urlStringBuilder.append(URLEncoder.encode(queryString, "UTF-8"));
        urlStringBuilder.append(mContext.getString(R.string.query_statement_tail)); // Finalize the URL for the API query
      } catch (UnsupportedEncodingException e) {
        saveAppStatus(AppStatus.STOCK_STATUS_ENCODING_ERROR);
        Log.d(LOG_TAG, "Error encoding the head of the query url.");
      }
    } else {
      try {
        urlStringBuilder.append(URLEncoder.encode(mContext.getString(R.string.query_statement_head), "UTF-8"));
      } catch (UnsupportedEncodingException e) {
        saveAppStatus(AppStatus.STOCK_STATUS_ENCODING_ERROR);
        Log.d(LOG_TAG, "Error encoding the head of the query url.");
      }

      if (!constructUrlForStocks(params, urlStringBuilder)) {
        return GcmNetworkManager.RESULT_FAILURE;
      }

      urlStringBuilder.append(mContext.getString(R.string.query_statement_tail)); // Finalize the URL for the API query
    }
    return queryServer(urlStringBuilder, isHistoric); // Query the server for those stocks
  }

  private int queryServer(StringBuilder urlStringBuilder, boolean historic) {
    String getResponse;
    int result = GcmNetworkManager.RESULT_FAILURE;

    if (urlStringBuilder != null) {
      getResponse = fetchData(urlStringBuilder.toString()); // Fetch data from server

      if (getResponse == null) {  // No valid data from the server for some reason
        return result; // No need to set an app status, fetchData method handles that
      }

      // Create batch operations to feed the db.
      // Set app status meaningfully when needed
      ArrayList<ContentProviderOperation> batchOperations = null;
      try {
        batchOperations = Utils.quoteJsonToContentVals(mContext, getResponse, historic);
      } catch (JSONException e) {
        saveAppStatus(AppStatus.STOCK_STATUS_INVALID_JSON_DATA);
        Log.d(LOG_TAG, "Error processing the json data from the server.", e);
      } catch (NonExistingStockException e) {
        saveAppStatus(AppStatus.STOCK_STATUS_NON_EXISTING_STOCK);
        Log.d(LOG_TAG, "Error querying a stock that does not exists.", e);
      }

      if (batchOperations == null) { // No batch operations gotten
        return result;
      }

      try {
        mContext.getContentResolver().applyBatch(
                QuoteProvider.AUTHORITY,
                batchOperations
        );
      } catch (RemoteException | OperationApplicationException e) {
        saveAppStatus(AppStatus.STOCK_STATUS_DATABASE_ERROR);
        Log.d(LOG_TAG, "Error applying batch insert with results from server.", e);
      }

      result = GcmNetworkManager.RESULT_SUCCESS;  // Everything correct? Then, operation successful
    }

    updateWidgets(); // At this point everything is ok

    return result;
  }

  private void updateWidgets(){
    Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED);
    dataUpdatedIntent.setPackage(mContext.getPackageName());
    mContext.sendBroadcast(dataUpdatedIntent);
  }

  private boolean constructUrlForStocks(TaskParams params, StringBuilder urlStringBuilder) {
    Cursor initQueryCursor;
    // "init" or "periodic" means a connection to the server based on
    // current records in the db
    if (isInit || isPeriodic) {
      initQueryCursor = mContext.getContentResolver().query(
              QuoteProvider.Quotes.CONTENT_URI,
              Projections.STOCK,
              null,
              null,
              null
      );
      // If records, then construct the url based on them
      if (initQueryCursor != null && initQueryCursor.getCount() > 0) {
        DatabaseUtils.dumpCursor(initQueryCursor); // Prints the records
        initQueryCursor.moveToFirst();
        for (int i = 0; i < initQueryCursor.getCount(); i++) {
          mStoredSymbols.append("\"" + initQueryCursor.getString(Projections.STOCK_SYMBOL) + "\",");
          initQueryCursor.moveToNext();
        }
        mStoredSymbols.replace(mStoredSymbols.length() - 1, mStoredSymbols.length(), ")"); // replace last comma
        try {
          urlStringBuilder.append(URLEncoder.encode(mStoredSymbols.toString(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
          saveAppStatus(AppStatus.STOCK_STATUS_ENCODING_ERROR);
          Log.d(LOG_TAG, "Error encoding the stocks to update.");
          return false;
        }
      } else {
        return false;
      }
    } else if (isAdd) {  // When tag is "add" the user is adding a new stock
      String stockInput = params.getExtras().getString(StockTaskService.SYMBOL_TAG); // get symbol from params.getExtra and build query
      try {
        urlStringBuilder.append(URLEncoder.encode("\"" + stockInput + "\")", "UTF-8")); // Url constructed based on that specific stock
      } catch (UnsupportedEncodingException e) {
        saveAppStatus(AppStatus.STOCK_STATUS_ENCODING_ERROR);
        Log.d(LOG_TAG, "Error encoding the new stock to query.");
        return false;
      }
    }

    return true;
  }

  private void saveAppStatus(@AppStatus.StockStatus int status) {
    Utils.setSharedPreference(
            mContext,
            mContext.getString(R.string.pref_key_stock_status),
            status,
            false
    );
  }
}
