package com.sam_chordas.android.stockhawk.service;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.RemoteException;
import android.util.Log;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.sam_chordas.android.stockhawk.AppStatus;
import com.sam_chordas.android.stockhawk.InvalidStockException;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.Projections;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;

import org.json.JSONException;

import java.io.BufferedReader;
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
public class StockTaskService extends GcmTaskService{
  private String LOG_TAG = StockTaskService.class.getSimpleName();

  private Context mContext;
  private StringBuilder mStoredSymbols = new StringBuilder();

  /*
      Used to update a record in the db
   */
  private boolean isUpdate;

  public StockTaskService(){}

  public StockTaskService(Context context){
    mContext = context;
  }

  private String fetchData(String url) {
    /*
        OkHttpClient was too troublesome
     */
    HttpURLConnection connection = null;
    BufferedReader reader = null;
    String stocksJson = null;

    try{
      URL stockUrl = new URL(url);
      connection = (HttpURLConnection) stockUrl.openConnection();
      connection.setRequestMethod("GET");
      connection.connect();

      InputStream stream = connection.getInputStream();

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

    } catch (Exception e) {
      saveAppStatus(AppStatus.STOCK_STATUS_NO_RESPONSE);
      Log.d(LOG_TAG, "Network error");
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
      if (reader != null) {
        try {
          reader.close();
        } catch (Exception e) {
          Log.e(LOG_TAG, "Error closing stream");
        }
      }
    }

    return stocksJson;
  }

  @Override
  public int onRunTask(TaskParams params){
    if (mContext == null){
      mContext = this;
    }

    /*
        Create the base url to query the server
     */
    StringBuilder urlStringBuilder = new StringBuilder();
    try{
      // Base URL for the Yahoo query
      urlStringBuilder.append(mContext.getString(R.string.query_base_url));
      urlStringBuilder.append(URLEncoder.encode(mContext.getString(R.string.query_statement_head), "UTF-8"));
    } catch (UnsupportedEncodingException e) {
      saveAppStatus(AppStatus.STOCK_STATUS_INTERNAL_ERROR);
      Log.d(LOG_TAG, "Invalid encoding");
    }

    /*
        Construct the first part of the url based on the stocks: default, in the db, new
     */
    if(!constructUrlForStocks(params, urlStringBuilder)){
      return GcmNetworkManager.RESULT_FAILURE;
    }

    // finalize the URL for the API query.
    urlStringBuilder.append(mContext.getString(R.string.query_statement_tail));

    /*
        Once the url is ready query the server for those stocks
     */
    return queryTheServer(urlStringBuilder);
  }

  private int queryTheServer(StringBuilder urlStringBuilder){
    String getResponse;
    int result = GcmNetworkManager.RESULT_FAILURE;

    if (urlStringBuilder != null){
      /*
          Fetch the data
       */
      getResponse = fetchData(urlStringBuilder.toString());

      /*
          No valid data from the server for some reason;
       */
      if(getResponse == null){
        // No need to set an app status, fetchData method handles that.
        return result;
      }

      /*
          Now, some meaningful data has been received, use it.
       */
      ContentValues contentValues = new ContentValues();
      // update ISCURRENT to 0 (false) so new data is current
      if (isUpdate){
        contentValues.put(QuoteColumns.ISCURRENT, 0);
        mContext.getContentResolver().update(
                QuoteProvider.Quotes.CONTENT_URI, contentValues,
                null,
                null
        );
      }

      /*
          Create batch operations to feed the db.
          Set app status meaningfully when needed
       */
      ArrayList<ContentProviderOperation> batchOperations = null;
      try {
         batchOperations = Utils.quoteJsonToContentVals(getResponse);
      } catch (JSONException e) {
        saveAppStatus(AppStatus.STOCK_STATUS_INVALID_DATA);
        Log.d(LOG_TAG, "Invalid json data from the server", e);
      } catch (InvalidStockException e) {
        saveAppStatus(AppStatus.STOCK_STATUS_INVALID_STOCK);
        Log.d(LOG_TAG, "Invalid stock requested", e);
      }

      /*
          No batch operations gotten
       */
      if(batchOperations == null){
        return result;
      }

      try {
        mContext.getContentResolver().applyBatch(
                QuoteProvider.AUTHORITY,
                batchOperations
        );
      } catch (RemoteException | OperationApplicationException e){
        saveAppStatus(AppStatus.STOCK_STATUS_INTERNAL_ERROR);
        Log.d(LOG_TAG, "Error applying batch insert", e);
      }

      /*
          Everything correct? Then, operation successful.
       */
      result = GcmNetworkManager.RESULT_SUCCESS;
    }

    return result;
  }

  private boolean constructUrlForStocks(TaskParams params, StringBuilder urlStringBuilder){
    Cursor initQueryCursor;
    /*
        "init" or "periodic" means a connection to the server based on
        current records in the db
     */
    if (params.getTag().equals("init") || params.getTag().equals("periodic")){
      isUpdate = true;
      initQueryCursor = mContext.getContentResolver().query(
              QuoteProvider.Quotes.CONTENT_URI,
              Projections.STOCK,
              null,
              null,
              null
      );
      /*
          If records, then construct the url based on them
      */
      if (initQueryCursor != null && initQueryCursor.getCount() > 0){
        DatabaseUtils.dumpCursor(initQueryCursor); // Prints the records
        initQueryCursor.moveToFirst();
        for (int i = 0; i < initQueryCursor.getCount(); i++){
          mStoredSymbols.append("\"" + initQueryCursor.getString(Projections.STOCK_SYMBOL) + "\",");
          initQueryCursor.moveToNext();
        }
        mStoredSymbols.replace(mStoredSymbols.length() - 1, mStoredSymbols.length(), ")"); // replace last comma
        try {
          urlStringBuilder.append(URLEncoder.encode(mStoredSymbols.toString(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
          saveAppStatus(AppStatus.STOCK_STATUS_INTERNAL_ERROR);
          Log.d(LOG_TAG, "Invalid encoding");
          return false;
        }
      } else {
        return false;
      }
    }
    /*
        When tag is "add" the user is adding a new stock
     */
    else if (params.getTag().equals("add")){
      isUpdate = false;
      // get symbol from params.getExtra and build query
      String stockInput = params.getExtras().getString("symbol");
      try {
        /*
            Url constructed based on that specific stock
         */
        urlStringBuilder.append(URLEncoder.encode("\"" + stockInput + "\")", "UTF-8"));
      } catch (UnsupportedEncodingException e){
        saveAppStatus(AppStatus.STOCK_STATUS_INTERNAL_ERROR);
        Log.d(LOG_TAG, "Invalid encoding");
        return false;
      }
    }

    return true;
  }

  private void saveAppStatus(@AppStatus.StockStatus int status){
    Utils.setSharedPreference(
            mContext,
            mContext.getString(R.string.pref_key_stock_status),
            AppStatus.STOCK_STATUS_INTERNAL_ERROR,
            false
    );
  }
}
