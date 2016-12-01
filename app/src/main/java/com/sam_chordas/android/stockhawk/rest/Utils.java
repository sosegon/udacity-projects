package com.sam_chordas.android.stockhawk.rest;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.sam_chordas.android.stockhawk.AppStatus;
import com.sam_chordas.android.stockhawk.InvalidStockException;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.PriceColumns;
import com.sam_chordas.android.stockhawk.data.Projections;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.model.HistoricPrice;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

/**
 * Created by sam_chordas on 10/8/15.
 */
public class Utils {

  private static String LOG_TAG = Utils.class.getSimpleName();

  public static boolean showPercent = true;

  // Throws exceptions to handle them and show meaningful comments to the user
  public static ArrayList quoteJsonToContentVals(Context context, String JSON, boolean historic) throws InvalidStockException, JSONException {
    ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
    JSONObject jsonObject = null;
    JSONArray resultsArray = null;

    jsonObject = new JSONObject(JSON);
    if (jsonObject != null && jsonObject.length() != 0) {
      jsonObject = jsonObject.getJSONObject("query");
      int count = Integer.parseInt(jsonObject.getString("count"));
      if (count == 1) {
        jsonObject = jsonObject.getJSONObject("results").getJSONObject("quote");
        addToBatchOperations(context, jsonObject, batchOperations, historic);
      } else {
        resultsArray = jsonObject.getJSONObject("results").getJSONArray("quote");
        if (resultsArray != null && resultsArray.length() != 0) {
          for (int i = 0; i < resultsArray.length(); i++) {
            jsonObject = resultsArray.getJSONObject(i);
            addToBatchOperations(context, jsonObject, batchOperations, historic);
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
    String symbol; // Get the symbol to create the uri to update the record
    try {
      symbol = jsonObject.getString("symbol").toUpperCase();
    } catch (JSONException e) {
      e.printStackTrace();
      return null;
    }

    ContentProviderOperation.Builder builder = ContentProviderOperation.newUpdate(
            QuoteProvider.Quotes.withSymbol(symbol));
    try {
      String change = jsonObject.getString("Change");
      builder.withValue(QuoteColumns.SYMBOL, symbol);
      builder.withValue(QuoteColumns.BIDPRICE, truncateBidPrice(jsonObject.getString("Bid")));
      builder.withValue(QuoteColumns.PERCENT_CHANGE, truncateChange(jsonObject.getString("ChangeinPercent"), true));
      builder.withValue(QuoteColumns.CHANGE, truncateChange(change, false));
      builder.withValue(QuoteColumns.ISCURRENT, 1);
      builder.withValue(QuoteColumns.ISTEMP, 0); // The data has been fetched from the server, record is not longer temp
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

  public static ContentProviderOperation buildHistoricBatchOperation(Context context, JSONObject jsonObject) {
    ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
            QuoteProvider.Prices.CONTENT_URI);
    try {
      long date = getDateInMilliSeconds(jsonObject.getString("Date"));
      float price = Float.valueOf(truncateBidPrice(jsonObject.getString("Open")));
      String symbol = jsonObject.getString("Symbol");
      HistoricPrice thePrice = new HistoricPrice(symbol, -1, date, price);
      if (priceExists(context, thePrice)) {
        return null;
      }

      builder.withValue(PriceColumns.STOCK_SYMBOL, symbol);
      builder.withValue(PriceColumns.DATE, date);
      builder.withValue(PriceColumns.PRICE, String.valueOf(price));
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

  public static void addToBatchOperations(Context context, JSONObject jsonObject, ArrayList<ContentProviderOperation> batchOperations, boolean historic)
          throws InvalidStockException, JSONException {
    if (isValidStock(jsonObject)) {
      if (historic) {
        ContentProviderOperation cpo = buildHistoricBatchOperation(context, jsonObject);
        if (cpo != null) {
          batchOperations.add(cpo);
        }
      } else {
        ContentProviderOperation cpo = buildBatchOperation(jsonObject);
        if (cpo != null) {
          batchOperations.add(cpo);
        }
      }
    } else {
      throw new InvalidStockException("Invalid stock: " + jsonObject.getString("symbol"));
    }
  }

  @SuppressWarnings("ResourceType")
  public static
  @AppStatus.StockStatus
  int getStockStatus(Context context) {
    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
    return pref.getInt(context.getString(R.string.pref_key_stock_status), AppStatus.STOCK_STATUS_UNKNOWN);
  }

  public static String getStockQueried(Context context) {
    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
    return pref.getString(context.getString(R.string.pref_key_stock_queried), "");
  }

  public static void setSharedPreference(Context context, String key, int value, boolean rightAway) {
    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
    SharedPreferences.Editor editor = pref.edit();
    editor.putInt(key, value);
    if (rightAway) {
      editor.apply();
    } else {
      editor.commit();
    }
  }

  public static void setSharedPreference(Context context, String key, String value, boolean rightAway) {
    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
    SharedPreferences.Editor editor = pref.edit();
    editor.putString(key, value);
    if (rightAway) {
      editor.apply();
    } else {
      editor.commit();
    }
  }

  public static void updateStockStatusView(Context context, TextView txt_status) {
    String message = context.getString(R.string.sta_no_stocks) + " ";

    @AppStatus.StockStatus int status = Utils.getStockStatus(context);

    switch (status) {
      case AppStatus.STOCK_STATUS_OK:
        txt_status.setVisibility(View.GONE);
        return;
      case AppStatus.STOCK_STATUS_UNKNOWN:
        message += context.getString(R.string.sta_unknown);
        break;
      case AppStatus.STOCK_STATUS_NO_CONNECTION:
        message += context.getString(R.string.sta_no_connection);
        break;
      case AppStatus.STOCK_STATUS_NO_RESPONSE:
        message += context.getString(R.string.sta_no_response);
        break;
      case AppStatus.STOCK_STATUS_INVALID_DATA:
        message += context.getString(R.string.sta_invalid_data);
        break;
      case AppStatus.STOCK_STATUS_INVALID_STOCK:
        message += context.getString(R.string.sta_invalid_stock, getStockQueried(context));
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        return;
      case AppStatus.STOCK_STATUS_ENCODING_ERROR:
        message += context.getString(R.string.sta_encoding_error);
        break;
      case AppStatus.STOCK_STATUS_DATABASE_ERROR:
        message += context.getString(R.string.sta_database_error);
        break;
      case AppStatus.STOCK_STATUS_NO_STOCKS:
        message += context.getString(R.string.sta_no_stocks_error);
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

  public static String getCurrentDate() {
    Calendar current = Calendar.getInstance();
    int year = current.get(Calendar.YEAR);
    int month = current.get(Calendar.MONTH) + 1;
    int day = current.get(Calendar.DAY_OF_MONTH);

    return year + "-" + month + "-" + day;
  }

  public static long getDateInMilliSeconds(String date) {

    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    //format.setTimeZone(TimeZone.getTimeZone("GMT+0:01")); // one minute ahead
    try {
      Date argDate = format.parse(date);
      return argDate.getTime();
    } catch (ParseException e) {
      Log.e(LOG_TAG, "Error when converting date to milliseconds");
    }

    return -1;
  }

  public static String getDateInSimpleFormat(long date) {
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(date);
    return format.format(calendar.getTime());
  }

  public static String getPreviousWeekDate(String date) {
    long milliseconds = getDateInMilliSeconds(date);
    long oneWeek = 604800000L; // 7 days

    return getDateInSimpleFormat(milliseconds - oneWeek);
  }

  public static String getPreviousMonthDate(String date) {
    long milliseconds = getDateInMilliSeconds(date);
    long oneMonth = 2592000000L; // 30 days

    return getDateInSimpleFormat(milliseconds - oneMonth);
  }

  public static String getPreviousYearDate(String date) {
    long milliseconds = getDateInMilliSeconds(date);
    long oneMonth = 31536000000L; // 365 days

    return getDateInSimpleFormat(milliseconds - oneMonth);
  }

  public static boolean priceExists(Context context, HistoricPrice record) {
    Cursor c = context.getContentResolver().query(
            QuoteProvider.Prices.historicPoint(record.getSymbol(), record.getDate()),
            Projections.HISTORIC,
            null,
            null,
            null
    );

    return c.moveToFirst();
  }
}
