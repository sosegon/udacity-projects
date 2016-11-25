package com.sam_chordas.android.stockhawk.rest;

import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.sam_chordas.android.stockhawk.AppStatus;
import com.sam_chordas.android.stockhawk.InvalidStockException;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by sam_chordas on 10/8/15.
 */
public class Utils {

  private static String LOG_TAG = Utils.class.getSimpleName();

  public static boolean showPercent = true;

  /*
      Throws exceptions to handle them and show meaningful comments to the user
   */
  public static ArrayList quoteJsonToContentVals(String JSON) throws InvalidStockException, JSONException{
    ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
    JSONObject jsonObject = null;
    JSONArray resultsArray = null;

    jsonObject = new JSONObject(JSON);
    if (jsonObject != null && jsonObject.length() != 0) {
      jsonObject = jsonObject.getJSONObject("query");
      int count = Integer.parseInt(jsonObject.getString("count"));
      if (count == 1) {
        jsonObject = jsonObject.getJSONObject("results").getJSONObject("quote");
        addToBatchOperations(jsonObject, batchOperations);
      } else {
        resultsArray = jsonObject.getJSONObject("results").getJSONArray("quote");
        if (resultsArray != null && resultsArray.length() != 0) {
          for (int i = 0; i < resultsArray.length(); i++) {
            jsonObject = resultsArray.getJSONObject(i);
            addToBatchOperations(jsonObject, batchOperations);
          }
        }
      }
    }

    return batchOperations;
  }

  public static String truncateBidPrice(String bidPrice) {
    bidPrice = String.format("%.2f", Float.parseFloat(bidPrice));
    return bidPrice;
  }

  public static String truncateChange(String change, boolean isPercentChange) {
    String weight = change.substring(0, 1);
    String ampersand = "";
    if (isPercentChange) {
      ampersand = change.substring(change.length() - 1, change.length());
      change = change.substring(0, change.length() - 1);
    }
    change = change.substring(1, change.length());
    double round = (double) Math.round(Double.parseDouble(change) * 100) / 100;
    change = String.format("%.2f", round);
    StringBuffer changeBuffer = new StringBuffer(change);
    changeBuffer.insert(0, weight);
    changeBuffer.append(ampersand);
    change = changeBuffer.toString();
    return change;
  }

  public static ContentProviderOperation buildBatchOperation(JSONObject jsonObject) {
    ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
            QuoteProvider.Quotes.CONTENT_URI);
    try {
      String change = jsonObject.getString("Change");
      builder.withValue(QuoteColumns.SYMBOL, jsonObject.getString("symbol"));
      builder.withValue(QuoteColumns.BIDPRICE, truncateBidPrice(jsonObject.getString("Bid")));
      builder.withValue(QuoteColumns.PERCENT_CHANGE, truncateChange(jsonObject.getString("ChangeinPercent"), true));
      builder.withValue(QuoteColumns.CHANGE, truncateChange(change, false));
      builder.withValue(QuoteColumns.ISCURRENT, 1);
      if (change.charAt(0) == '-') {
        builder.withValue(QuoteColumns.ISUP, 0);
      } else {
        builder.withValue(QuoteColumns.ISUP, 1);
      }

    } catch (JSONException e) {
      e.printStackTrace();
    }
    return builder.build();
  }

  public static boolean isValidStock(JSONObject jsonQuote) throws JSONException {
    Iterator<String> ite = jsonQuote.keys();
    while (ite.hasNext()) {
      String currentKey = ite.next();
      if (currentKey.toLowerCase().equals("symbol")) { // To lower case 'cause there are 'symbol' and 'Symbol'
        continue;
      }
      String currentValue = jsonQuote.getString(currentKey);
      if (!(currentValue == null || currentValue.equals("null"))) {
        return true;
      }
    }

    /*
        Stock is invalid when all the fields except 'symbol' are null
     */
    return false;
  }

  public static void addToBatchOperations(JSONObject jsonObject, ArrayList<ContentProviderOperation> batchOperations)
          throws InvalidStockException, JSONException{
    if (isValidStock(jsonObject)) {
      batchOperations.add(buildBatchOperation(jsonObject));
    } else {
      throw new InvalidStockException("Invalid stock: " + jsonObject.getString("symbol"));
    }
  }

  @SuppressWarnings("ResourceType")
  public static @AppStatus.StockStatus int getStockStatus(Context context){
    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
    return pref.getInt(context.getString(R.string.pref_key_stock_status), AppStatus.STOCK_STATUS_UNKNOWN);
  }

  public static String getStockQueried(Context context){
    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
    return pref.getString(context.getString(R.string.pref_key_stock_queried), "");
  }

  public static void setSharedPreference(Context context, String key, int value, boolean rightAway){
    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
    SharedPreferences.Editor editor = pref.edit();
    editor.putInt(key, value);
    if(rightAway){
      editor.apply();
    }
    else {
      editor.commit();
    }
  }

  public static void setSharedPreference(Context context, String key, String value, boolean rightAway){
    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
    SharedPreferences.Editor editor = pref.edit();
    editor.putString(key, value);
    if(rightAway){
      editor.apply();
    }
    else {
      editor.commit();
    }
  }

  public static void updateStockStatusView(Context context, TextView txt_status){
    String message = context.getString(R.string.sta_no_stocks);

    @AppStatus.StockStatus int status = Utils.getStockStatus(context);

    switch (status){
      case AppStatus.STOCK_STATUS_OK:
        txt_status.setVisibility(View.GONE);
        return;
      case AppStatus.STOCK_STATUS_UNKNOWN:
        message += " " + context.getString(R.string.sta_unknown);
        break;
      case AppStatus.STOCK_STATUS_NO_CONNECTION:
        message += " " + context.getString(R.string.sta_no_connection);
        break;
      case AppStatus.STOCK_STATUS_NO_RESPONSE:
        message += " " + context.getString(R.string.sta_no_response);
        break;
      case AppStatus.STOCK_STATUS_BAD_REQUEST:
        message += " " + context.getString(R.string.sta_bad_response);
        break;
      case AppStatus.STOCK_STATUS_INVALID_DATA:
        message += " " + context.getString(R.string.sta_invalid_data);
        break;
      case AppStatus.STOCK_STATUS_INVALID_STOCK:
        message += " " + context.getString(R.string.sta_invalid_stock, getStockQueried(context));
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        return;
      case AppStatus.STOCK_STATUS_INTERNAL_ERROR:
        message += " " + context.getString(R.string.sta_internal_error);
        break;
      case AppStatus.STOCK_STATUS_NETWORK_ERROR:
        message += " " + context.getString(R.string.sta_network_error);
        break;
      default:
        break;
    }

    txt_status.setText(message);
    txt_status.setVisibility(View.VISIBLE);
  }

  public static boolean isNetworkAvailable(Context context) {
    ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo netInfo = manager.getActiveNetworkInfo();

    return netInfo != null && netInfo.isConnectedOrConnecting();
  }

  public static void goLoader(AppCompatActivity activity, int loaderId, LoaderManager.LoaderCallbacks callbacks) {

    /*
        Check the existence of the fragment to avoid creating a new one,
        and just restart the previous one.
     */
    if (activity.getSupportLoaderManager().getLoader(loaderId) == null) {
      activity.getSupportLoaderManager().initLoader(loaderId, null, callbacks);
    } else {
      activity.getSupportLoaderManager().restartLoader(loaderId, null, callbacks);
    }
  }
}
