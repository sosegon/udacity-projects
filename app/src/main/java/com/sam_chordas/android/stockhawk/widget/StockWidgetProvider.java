package com.sam_chordas.android.stockhawk.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.ui.MyStocksActivity;

/**
 * Created by sebastian on 12/8/16.
 */
public class StockWidgetProvider extends AppWidgetProvider{

  @Override
  public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    super.onUpdate(context, appWidgetManager, appWidgetIds);
    for(int appWidgetId : appWidgetIds){
      RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_stock);

      Intent launchIntent = new Intent(context, MyStocksActivity.class);
      PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, launchIntent, 0);
      views.setOnClickPendingIntent(R.id.ll_w, pendingIntent);

      appWidgetManager.updateAppWidget(appWidgetId, views);
    }
  }
}
