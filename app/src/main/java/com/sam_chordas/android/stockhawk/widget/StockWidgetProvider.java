package com.sam_chordas.android.stockhawk.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.sam_chordas.android.stockhawk.service.StockTaskService;
import com.sam_chordas.android.stockhawk.service.StockWidgetIntentService;

/**
 * Created by sebastian on 12/8/16.
 */
public class StockWidgetProvider extends AppWidgetProvider{

  @Override
  public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    context.startService(new Intent(context, StockWidgetIntentService.class));
  }

  @Override
  public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
    context.startService(new Intent(context, StockWidgetIntentService.class));
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    super.onReceive(context, intent);
    if(StockTaskService.ACTION_DATA_UPDATED.equals(intent.getAction())){
      context.startService(new Intent(context, StockWidgetIntentService.class));
    }
  }
}
