package com.sam_chordas.android.stockhawk.rest;

import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.db.chart.model.ChartSet;
import com.db.chart.model.LineSet;
import com.db.chart.view.ChartView;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.Projections;

import java.util.ArrayList;

/**
 * Created by sebastian on 11/30/16.
 */
public class HistoricPriceCursorAdapter extends BaseAdapter {

  public static class ViewHolder {
    public TextView txt_stock_name;
    public TextView txt_stock_high;
    public TextView txt_stock_low;
    public TextView txt_date_low;
    public TextView txt_date_high;
    public ChartView lch_history;

    public ViewHolder(View view) {
      txt_stock_name = (TextView) view.findViewById(R.id.txt_stock_name);
      txt_stock_high = (TextView) view.findViewById(R.id.txt_stock_high);
      txt_stock_low = (TextView) view.findViewById(R.id.txt_stock_low);
      txt_date_high = (TextView) view.findViewById(R.id.txt_date_high);
      txt_date_low = (TextView) view.findViewById(R.id.txt_date_low);
      lch_history = (ChartView) view.findViewById(R.id.lch_history);
    }
  }

  private static final String LOG_TAG = HistoricPriceCursorAdapter.class.getSimpleName();
  private ViewHolder mHolder;
  private DataSetObserver mDataSetObserver;
  private Context mContext;
  private String mStockName;
  private String mStockNameSpell;

  private Cursor mCursor;

  public HistoricPriceCursorAdapter(Context context, View view, Cursor cursor) {
    super();
    mContext = context;
    mHolder = new ViewHolder(view);
    mCursor = cursor;
    mDataSetObserver = new NotifyingDataSetObserver();
  }

  public Cursor swapCursor(Cursor newCursor) {
    if (newCursor == null || newCursor == mCursor || newCursor.getCount() == 0) {
      return null;
    }
    final Cursor oldCursor = mCursor;
    if (oldCursor != null && mDataSetObserver != null) {
      oldCursor.unregisterDataSetObserver(mDataSetObserver);
    }
    mCursor = newCursor;
    if (mCursor != null) {
      if (mDataSetObserver != null) {
        mCursor.registerDataSetObserver(mDataSetObserver);
      }
    } else {
    }

    updateView();

    return oldCursor;
  }

  @Override
  public Object getItem(int i) {
    return null;
  }

  @Override
  public View getView(int i, View view, ViewGroup viewGroup) {
    return null;
  }

  @Override
  public int getCount() {
    return 0;
  }

  @Override
  public long getItemId(int i) {
    return 0;
  }

  private class NotifyingDataSetObserver extends DataSetObserver {
    @Override
    public void onChanged() {
      super.onChanged();
      notifyDataSetChanged();
    }

    @Override
    public void onInvalidated() {
      super.onInvalidated();
      notifyDataSetChanged();
    }
  }

  private void updateMaxPrice() {
    mCursor.moveToFirst();
    float max = mCursor.getFloat(Projections.HISTORIC_PRICE);
    int c = mCursor.getPosition();
    while (mCursor.moveToNext()) {
      float val = mCursor.getFloat(Projections.HISTORIC_PRICE);
      c = val > max ? mCursor.getPosition() : c;
      max = val > max ? val : max;
    }

    String value = String.valueOf(max);
    mCursor.moveToPosition(c);
    String date = Utils.getFriendlyDayString(mContext, mCursor.getLong(Projections.HISTORIC_DATE));
    mHolder.txt_stock_high.setText(value);
    mHolder.txt_date_high.setText(date);

    mHolder.txt_stock_high.setContentDescription(
            mContext.getString(R.string.desc_stock_max_price, mStockNameSpell, value)
    );
    mHolder.txt_date_high.setContentDescription(
            mContext.getString(R.string.desc_stock_max_date, mStockNameSpell, date)
    );

  }

  private void updateMinPrice() {
    mCursor.moveToFirst();
    float min = mCursor.getFloat(Projections.HISTORIC_PRICE);
    int c = mCursor.getPosition();
    while (mCursor.moveToNext()) {
      float val = mCursor.getFloat(Projections.HISTORIC_PRICE);
      c = val < min ? mCursor.getPosition() : c;
      min = val < min ? val : min;
    }

    String value = String.valueOf(min);
    mCursor.moveToPosition(c);
    String date = Utils.getFriendlyDayString(mContext, mCursor.getLong(Projections.HISTORIC_DATE));
    mHolder.txt_stock_low.setText(value);
    mHolder.txt_date_low.setText(date);

    mHolder.txt_stock_low.setContentDescription(
            mContext.getString(R.string.desc_stock_min_price, mStockNameSpell, value)
    );
    mHolder.txt_date_low.setContentDescription(
            mContext.getString(R.string.desc_stock_min_date, mStockNameSpell, date)
    );
  }

  private void updateStockName() {
    mCursor.moveToFirst();
    mStockName = (mCursor.getString(Projections.HISTORIC_STOCK_SYMBOL));
    mStockNameSpell = Utils.spellWord(mStockName);
    mHolder.txt_stock_name.setText(mStockName);
    mHolder.txt_stock_name.setContentDescription(
            mContext.getString(R.string.desc_stock_name, mStockNameSpell)
    );
  }

  private void updateView(){
    // Update the text views with the data from the cursor
    updateStockName();
    updateMaxPrice();
    updateMinPrice();

    // TODO: Check this so the number of points when swapping cursors is the same
    // This leads to errors when the app loads data from the content provider, then
    // queries the server and updates the content provider. The solution may be empty
    // the line set before adding new data.
    LineSet lineSet = new LineSet();
    lineSet.setColor(mContext.getResources().getColor(R.color.colorPrimaryText));
    int c = 0;
    mCursor.moveToFirst();
    while (mCursor.moveToNext()) {
      lineSet.addPoint(String.valueOf(c), mCursor.getFloat(Projections.HISTORIC_PRICE));
      c++;
    }

    // This is a workaround to avoid errors when adding new data
    ArrayList<ChartSet> cs = new ArrayList<>();
    cs.add(lineSet);

    mHolder.lch_history.addData(cs);
    mHolder.lch_history.show();
  }
}
