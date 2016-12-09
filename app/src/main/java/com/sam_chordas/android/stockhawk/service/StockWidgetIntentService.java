package com.sam_chordas.android.stockhawk.service;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.RemoteViews;

import com.google.android.gms.gcm.TaskParams;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.Projections;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
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
    int isUp = c.getInt(Projections.STOCK_ISUP);

    c.close();

    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
    int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, StockWidgetProvider.class));

    for(int appWidgetId : appWidgetIds){
      RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_stock);

      views.setTextViewText(R.id.txt_w_name, stockName);
      views.setTextViewText(R.id.txt_w_price, stockPrice);

      Intent launchIntent = new Intent(this, MyStocksActivity.class);
      PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
      views.setOnClickPendingIntent(R.id.ll_w, pendingIntent);

      appWidgetManager.updateAppWidget(appWidgetId, views);
    }
  }
}
