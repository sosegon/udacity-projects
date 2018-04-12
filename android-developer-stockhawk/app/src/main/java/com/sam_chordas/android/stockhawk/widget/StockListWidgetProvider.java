package com.sam_chordas.android.stockhawk.widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.service.StockListWidgetRemoteViewsService;
import com.sam_chordas.android.stockhawk.service.StockTaskService;
import com.sam_chordas.android.stockhawk.ui.LineGraphActivity;
import com.sam_chordas.android.stockhawk.ui.MyStocksActivity;

/**
 * Created by sebastian on 12/10/16.
 */
public class StockListWidgetProvider extends AppWidgetProvider {

  @Override
  public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    // Iterate over each widget belonging to this provider
    for (int appWidgetId : appWidgetIds) {
      RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_stock_list);

      // This intent launches the stocks activity
      Intent intent = new Intent(context, MyStocksActivity.class);
      PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
      views.setOnClickPendingIntent(R.id.gro_w_header, pendingIntent);

      int sdk = Build.VERSION.SDK_INT;

      // Connect remote views to the remote views factory
      // Remote views factory is the equivalent to cursor adapter but for remote views
      // Setting the remote adapter causes the lifecycle of the factory to start which in turns
      // causes that the data is reloaded and remote views created for each row
      if (sdk >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
        setRemoteAdapter(context, views);
      } else {
        setRemoteAdapterV11(context, views);
      }

      // Here is set the common template for the pending intent for all the list items
      // This common intent allows to share several things like the activity to start
      // when clicking on an item
      // Then, each list item has its unique parts, this is handled by the remote views
      // service under getAtView method
      Intent clickIntentTemplate = new Intent(context, LineGraphActivity.class);
      PendingIntent clickPendingIntentTemplate = TaskStackBuilder.create(context)
              .addNextIntentWithParentStack(clickIntentTemplate)
              .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
      views.setPendingIntentTemplate(R.id.gro_w_stocks, clickPendingIntentTemplate);
      views.setEmptyView(R.id.gro_w_stocks, R.id.txt_w_empty);

      // Tell the AppWidgetManager to perform an update on the current app widget
      appWidgetManager.updateAppWidget(appWidgetId, views);
    }
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    super.onReceive(context, intent);
    if (StockTaskService.ACTION_DATA_UPDATED.equals(intent.getAction())) {
      AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
      int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, getClass()));
      appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.gro_w_stocks);
    }
  }

  @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
  private void setRemoteAdapter(Context context, @NonNull final RemoteViews views) {
    views.setRemoteAdapter(R.id.gro_w_stocks, new Intent(context, StockListWidgetRemoteViewsService.class));
  }

  @SuppressWarnings("deprecation")
  private void setRemoteAdapterV11(Context context, @NonNull final RemoteViews views) {
    views.setRemoteAdapter(0, R.id.gro_w_stocks, new Intent(context, StockListWidgetRemoteViewsService.class));
  }
}
