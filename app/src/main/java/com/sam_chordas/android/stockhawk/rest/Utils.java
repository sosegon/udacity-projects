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
import android.text.format.Time;
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

  public static ContentProviderOperation buildBatchOperation(JSONObject jsonObject) throws JSONException {
    String symbol; // Get the symbol to create the uri to update the record
    String change, bidPrice, percentChange;
    try {
      symbol = jsonObject.getString("symbol").toUpperCase();
      change = jsonObject.getString("Change");
      bidPrice = jsonObject.getString("Bid");
      percentChange = jsonObject.getString("ChangeinPercent");
    } catch (JSONException e) {
      e.printStackTrace();
      return null;
    }

    if(symbol == null || symbol.equals("null") ||
            change == null || change.equals("null") ||
            bidPrice == null || bidPrice.equals("null") ||
            percentChange == null || percentChange.equals("null") ||
            change == null || change.equals("null")) {
      // When any of the useful values of the stock is null,
      // then the information is incomplete. Therefore, the data
      // is invalid, even though the data is well parsed by the
      // json interpreter
      throw new JSONException("Invalid fields in stock");
    }

    ContentProviderOperation.Builder builder = ContentProviderOperation.newUpdate(
            QuoteProvider.Quotes.withSymbol(symbol));
    builder.withValue(QuoteColumns.SYMBOL, symbol);
    builder.withValue(QuoteColumns.BIDPRICE, truncateBidPrice(bidPrice));
    builder.withValue(QuoteColumns.PERCENT_CHANGE, truncateChange(percentChange, true));
    builder.withValue(QuoteColumns.CHANGE, truncateChange(change, false));
    builder.withValue(QuoteColumns.ISCURRENT, 1);
    builder.withValue(QuoteColumns.ISTEMP, 0); // The data has been fetched from the server, record is not longer temp
    if (change.charAt(0) == '-') {
      builder.withValue(QuoteColumns.ISUP, 0);
    } else {
      builder.withValue(QuoteColumns.ISUP, 1);
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

  public static int getQueryingHistoric(Context context) {
    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
    return pref.getInt(context.getString(R.string.pref_key_querying_historic), 0);
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

  public static void updateStockStatusView(Context context, TextView txt_status, boolean stocksInDb) {
    String message = "";

    @AppStatus.StockStatus int status = Utils.getStockStatus(context);

    txt_status.setVisibility(View.GONE);
    switch (status) {
      case AppStatus.STOCK_STATUS_OK:
        return;
      case AppStatus.STOCK_STATUS_UNKNOWN:
        message += context.getString(R.string.sta_unknown);
        break;
      case AppStatus.STOCK_STATUS_NO_CONNECTION:
        message += context.getString(R.string.sta_no_connection);
        if(stocksInDb){
          message += " " + context.getString(R.string.sta_no_latest);
        }
        break;
      case AppStatus.STOCK_STATUS_NO_RESPONSE:
        message += context.getString(R.string.sta_no_response);
        if(stocksInDb){
          message += " " + context.getString(R.string.sta_no_latest);
        }
        break;
      case AppStatus.STOCK_STATUS_INVALID_DATA:
        message += context.getString(R.string.sta_invalid_data);
        break;
      case AppStatus.STOCK_STATUS_INVALID_STOCK:
        message += context.getString(R.string.sta_invalid_stock, getStockQueried(context));
        break;
      case AppStatus.STOCK_STATUS_ENCODING_ERROR:
        message += context.getString(R.string.sta_encoding_error);
        break;
      case AppStatus.STOCK_STATUS_DATABASE_ERROR:
        message += context.getString(R.string.sta_database_error);
        break;
      case AppStatus.STOCK_STATUS_NO_DATA:
        message += context.getString(R.string.sta_no_data);
        txt_status.setText(message);
        txt_status.setVisibility(View.VISIBLE);
        return;
      default:
        break;
    }

    Toast.makeText(context, message, Toast.LENGTH_LONG).show();
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

  // Format used for storing dates in the database.  ALso used for converting those strings
  // back into date objects for comparison/processing.
  public static final String DATE_FORMAT = "yyyyMMdd";

  // From https://github.com/udacity/Advanced_Android_Development/blob/6.07_So_Much_Real_Estate_Part_1_Start/app/src/main/java/com/example/android/sunshine/app/Utility.java#L75
  public static String getFriendlyDayString(Context context, long dateInMillis) {
    // The day string for forecast uses the following logic:
    // For today: "Today, June 8"
    // For tomorrow:  "Tomorrow"
    // For the next 5 days: "Wednesday" (just the day name)
    // For all days after that: "Mon Jun 8"

    Time time = new Time();
    time.setToNow();
    long currentTime = System.currentTimeMillis();
    int julianDay = Time.getJulianDay(dateInMillis, time.gmtoff);
    int currentJulianDay = Time.getJulianDay(currentTime, time.gmtoff);

    // If the date we're building the String for is today's date, the format
    // is "Today, June 24"
    if (julianDay == currentJulianDay) {
      String today = context.getString(R.string.today);
      int formatId = R.string.format_full_friendly_date;
      return String.format(context.getString(
              formatId,
              today,
              getFormattedMonthDay(context, dateInMillis)));
    } else if ( currentJulianDay < julianDay - 7 ) {
      // If the input date is less than a week in the future, just return the day name.
      return getDayName(context, dateInMillis);
    } else {
      // Otherwise, use the form "Mon Jun 3"
      SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
      return shortenedDateFormat.format(dateInMillis);
    }
  }

  // From https://github.com/udacity/Advanced_Android_Development/blob/6.07_So_Much_Real_Estate_Part_1_Start/app/src/main/java/com/example/android/sunshine/app/Utility.java#L133
  public static String getDayName(Context context, long dateInMillis) {
    // If the date is today, return the localized version of "Today" instead of the actual
    // day name.

    Time t = new Time();
    t.setToNow();
    int julianDay = Time.getJulianDay(dateInMillis, t.gmtoff);
    int currentJulianDay = Time.getJulianDay(System.currentTimeMillis(), t.gmtoff);
    if (julianDay == currentJulianDay) {
      return context.getString(R.string.today);
    } else if ( julianDay == currentJulianDay - 1 ) {
      return context.getString(R.string.yesterday);
    } else {
      Time time = new Time();
      time.setToNow();
      // Otherwise, the format is just the day of the week (e.g "Wednesday".
      SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
      return dayFormat.format(dateInMillis);
    }
  }

  // From https://github.com/udacity/Advanced_Android_Development/blob/6.07_So_Much_Real_Estate_Part_1_Start/app/src/main/java/com/example/android/sunshine/app/Utility.java#L161
  public static String getFormattedMonthDay(Context context, long dateInMillis ) {
    Time time = new Time();
    time.setToNow();
    SimpleDateFormat dbDateFormat = new SimpleDateFormat(Utils.DATE_FORMAT);
    SimpleDateFormat monthDayFormat = new SimpleDateFormat("MMMM dd");
    String monthDayString = monthDayFormat.format(dateInMillis);
    return monthDayString;
  }

  public static String spellWord(String word){
    String spell = "";
    char[] letters =  word.toCharArray();
    for(char l : letters){
      spell += l + " ";
    }
    return spell;
  }

  // update current stocks isTemp and isCurrent values for UI purposes
  // The idea is that when the application starts or resumes, the records
  // in the db are updated right away with values of temp and current that are
  // used to modify the view, so the users gets an idea of what is going on.
  // When temp is 1, then a progress bar is displayed
  // When current is 0, the value of the stock price is italic
  public static  ArrayList<ContentProviderOperation> updateStocksUIPurpose(Cursor c, int temp, int current) {
    ArrayList<ContentProviderOperation> cpo = new ArrayList<>();

    if(c == null || c.getCount() == 0) {
      return cpo;
    }

    while(c.moveToNext()) {
      String symbol = c.getString(Projections.STOCK_SYMBOL);
      ContentProviderOperation.Builder builder = ContentProviderOperation.newUpdate(
              QuoteProvider.Quotes.withSymbol(symbol)
      );

      builder.withValue(QuoteColumns.ISTEMP, temp);
      builder.withValue(QuoteColumns.ISCURRENT, current);

      cpo.add(builder.build());
    }

    return cpo;
  }
}
