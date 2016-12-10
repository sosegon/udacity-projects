package com.sam_chordas.android.stockhawk.service;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.google.android.gms.gcm.TaskParams;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.Projections;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.ui.MyStocksActivity;
import com.sam_chordas.android.stockhawk.widget.StockWidgetProvider;

/**
 * Created by sebastian on 12/9/16.
 */
public class StockWidgetIntentService extends IntentService {

  public final static String LOG_TAG = StockWidgetIntentService.class.getSimpleName();

  public StockWidgetIntentService() {
    super(StockIntentService.class.getName());
  }

  @Override
  protected void onHandleIntent(Intent intent) {

    StockTaskService stockTaskService = new StockTaskService(this);
    int result = stockTaskService.onRunTask(new TaskParams("init", new Bundle()));

    Cursor c = getContentResolver().query(
            QuoteProvider.Quotes.CONTENT_URI,
            Projections.STOCK,
            null,
            null,
            null
    );

    if(c == null){
      return;
    }

    if(!c.moveToFirst()){
      c.close(); // prevent leaks
      return;
    }

    String stockName = c.getString(Projections.STOCK_SYMBOL);
    String stockPrice = c.getString(Projections.STOCK_BIDPRICE);
    String changePercentage = c.getString(Projections.STOCK_PERCENT_CHANGE);
    int isUp = c.getInt(Projections.STOCK_ISUP);

    c.close();

    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
    int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, StockWidgetProvider.class));

    for(int appWidgetId : appWidgetIds){
      int widgetWidth = getWidgetWidth(appWidgetManager, appWidgetId);
      int defaultWidth = getResources().getDimensionPixelSize(R.dimen.widget_stock_default_width);
      int layoutId;

      if(widgetWidth <= defaultWidth){
        layoutId = R.layout.widget_stock;
      } else {
        layoutId = R.layout.widget_stock_large;
      }

      RemoteViews views = new RemoteViews(getPackageName(), layoutId);

      views.setTextViewText(R.id.txt_w_name, stockName);
      views.setTextViewText(R.id.txt_w_price, stockPrice);
      views.setTextViewText(R.id.txt_w_change, changePercentage);

      int sdk = Build.VERSION.SDK_INT;
      if (isUp == 1){
        if (sdk < Build.VERSION_CODES.JELLY_BEAN){
          views.setInt(R.id.txt_w_change, "setBackgroundDrawable", R.drawable.percent_change_pill_green);
        }else {
          views.setInt(R.id.txt_w_change, "setBackgroundResource", R.drawable.percent_change_pill_green);
        }
      } else{
        if (sdk < Build.VERSION_CODES.JELLY_BEAN) {
          views.setInt(R.id.txt_w_change, "setBackgroundDrawable", R.drawable.percent_change_pill_red);
        } else{
          views.setInt(R.id.txt_w_change, "setBackgroundResource", R.drawable.percent_change_pill_red);
        }
      }

      // Content Descriptions for RemoteViews were only added in ICS MR1
      if (sdk >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
        views.setContentDescription(R.id.txt_w_name, getString(R.string.desc_stock_name, Utils.spellWord(stockName)));
        views.setContentDescription(R.id.txt_w_price, getString(R.string.desc_stock_current_price, Utils.spellWord(stockPrice)));
        views.setContentDescription(R.id.txt_w_change, getString(R.string.desc_stock_percentage_change, Utils.spellWord(changePercentage)));
      }

      Intent launchIntent = new Intent(this, MyStocksActivity.class);
      PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
      views.setOnClickPendingIntent(R.id.ll_w, pendingIntent);

      appWidgetManager.updateAppWidget(appWidgetId, views);
    }
  }

  // From https://github.com/udacity/Advanced_Android_Development/blob/2e1698d0f55639af434dc8d96bd7e11d734537b9/app/src/main/java/com/example/android/sunshine/app/widget/TodayWidgetIntentService.java#L124
  private int getWidgetWidth(AppWidgetManager appWidgetManager, int appWidgetId){
    // Previous to Jelly Bean, widgets were always their default size
    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN){
      return getResources().getDimensionPixelSize(R.dimen.widget_stock_default_width);
    }

    return getWidgetWidthFromOptions(appWidgetManager, appWidgetId);
  }

  // From https://github.com/udacity/Advanced_Android_Development/blob/2e1698d0f55639af434dc8d96bd7e11d734537b9/app/src/main/java/com/example/android/sunshine/app/widget/TodayWidgetIntentService.java#L135
  @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
  private int getWidgetWidthFromOptions(AppWidgetManager appWidgetManager, int appWidgetId){
    Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);

    if(options.containsKey(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)){
      int minWidthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);

      DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

      return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, minWidthDp, displayMetrics);
    }

    return  getResources().getDimensionPixelSize(R.dimen.widget_stock_default_width);
  }

}
