package com.sam_chordas.android.stockhawk.service;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.Projections;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.ui.LineGraphActivity;

/**
 * Created by sebastian on 12/10/16.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class StockListWidgetRemoteViewsService extends RemoteViewsService {
  @Override
  public RemoteViewsFactory onGetViewFactory(Intent intent) {
    return new RemoteViewsFactory() {
      private Cursor data = null;

      @Override
      public void onCreate() {

      }

      @Override
      public void onDataSetChanged() {
        if (data != null) {
          data.close(); // avoid leaks
        }

        // This method is called by the app hosting the widget (e.g., the launcher)
        // However, our ContentProvider is not exported so it doesn't have access to the
        // data. Therefore we need to clear (and finally restore) the calling identity so
        // that calls use our process and permission
        final long identityToken = Binder.clearCallingIdentity();

        data = getContentResolver().query(
                QuoteProvider.Quotes.CONTENT_URI,
                Projections.STOCK,
                null,
                null,
                null
        );

        Binder.restoreCallingIdentity(identityToken);
      }

      @Override
      public void onDestroy() {
        if (data != null) {
          data.close(); // avoid leaks
          data = null;
        }
      }

      @Override
      public int getCount() {
        return data != null ? data.getCount() : 0;
      }

      // Here is where every element in the list is set properly to display the corresponding
      // data. It's the equivalent to onBindViewHolder of the cursor adapter
      @Override
      public RemoteViews getViewAt(int i) {
        if (i == AdapterView.INVALID_POSITION ||
                data == null ||
                !data.moveToPosition(i)) {
          return null;
        }

        String stockName = data.getString(Projections.STOCK_SYMBOL);
        String stockPrice = data.getString(Projections.STOCK_BIDPRICE);
        String changePercentage = data.getString(Projections.STOCK_PERCENT_CHANGE);
        int isUp = data.getInt(Projections.STOCK_ISUP);

        RemoteViews views = new RemoteViews(
                getPackageName(),
                R.layout.widget_stock_list_item
        );

        views.setTextViewText(R.id.txt_w_name, stockName);
        views.setTextViewText(R.id.txt_w_price, stockPrice);
        views.setTextViewText(R.id.txt_w_change, changePercentage);

        int sdk = Build.VERSION.SDK_INT;
        if (isUp == 1) {
          if (sdk < Build.VERSION_CODES.JELLY_BEAN) {
            views.setInt(R.id.txt_w_change, "setBackgroundDrawable", R.drawable.percent_change_pill_green);
          } else {
            views.setInt(R.id.txt_w_change, "setBackgroundResource", R.drawable.percent_change_pill_green);
          }
        } else {
          if (sdk < Build.VERSION_CODES.JELLY_BEAN) {
            views.setInt(R.id.txt_w_change, "setBackgroundDrawable", R.drawable.percent_change_pill_red);
          } else {
            views.setInt(R.id.txt_w_change, "setBackgroundResource", R.drawable.percent_change_pill_red);
          }
        }

        // Content Descriptions for RemoteViews were only added in ICS MR1
        if (sdk >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
          views.setContentDescription(R.id.txt_w_name, getString(R.string.desc_stock_name, Utils.spellWord(stockName)));
          views.setContentDescription(R.id.txt_w_price, getString(R.string.desc_stock_current_price, Utils.spellWord(stockPrice)));
          views.setContentDescription(R.id.txt_w_change, getString(R.string.desc_stock_percentage_change, Utils.spellWord(changePercentage)));
        }

        // Here, the unique part (uri) is set so the list item acts properly
        // when clicking on it
        final Intent fillInIntent = new Intent();
        fillInIntent.putExtra(LineGraphActivity.STOCK_NAME_TAG, stockName);

        views.setOnClickFillInIntent(R.id.gro_w_stock_item, fillInIntent);
        return views;
      }

      @Override
      public RemoteViews getLoadingView() {
        return new RemoteViews(getPackageName(), R.layout.widget_stock_list_item);
      }

      @Override
      public int getViewTypeCount() {
        return 1;
      }

      @Override
      public long getItemId(int i) {
        return data.moveToPosition(i) ? data.getLong(Projections.STOCK_ID) : i;
      }

      @Override
      public boolean hasStableIds() {
        return true;
      }
    };
  }
}
