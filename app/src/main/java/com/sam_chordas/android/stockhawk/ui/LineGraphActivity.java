package com.sam_chordas.android.stockhawk.ui;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ViewGroup;

import com.db.chart.renderer.AxisRenderer;
import com.db.chart.view.LineChartView;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.Projections;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.HistoricPriceCursorAdapter;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.service.StockIntentService;

/**
 * Created by sebastian on 11/26/16.
 */
public class LineGraphActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

  private static final String LOG_TAG = LineGraphActivity.class.getSimpleName();
  private String mStockName;
  private Intent mServiceIntent;
  private static final int CURSOR_LOADER_ID = 0;
  private HistoricPriceCursorAdapter mCursorAdapter;
  private LineChartView linechart;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_line_graph);

    configChart();

    // Unlike a recycler view where the adapter is associated to the view,
    // Here the situation is the opposite, the view is associated to the adapter
    final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) this.findViewById(android.R.id.content)).getChildAt(0);
    mCursorAdapter = new HistoricPriceCursorAdapter(this, viewGroup, null);

    mServiceIntent = new Intent(this, StockIntentService.class);
    if(getIntent() != null){
      mStockName = getIntent().getStringExtra("stockName");

      goLoader();

      mServiceIntent.putExtra("tag", "historic");
      mServiceIntent.putExtra("symbol", mStockName);
      startService(mServiceIntent);
    }
  }

  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    String currentDate = Utils.getCurrentDate(),
            previousMonthDate = Utils.getPreviousMonthDate(currentDate);

    long cDate = Utils.getDateInMilliSeconds(currentDate),
            pDate = Utils.getDateInMilliSeconds(previousMonthDate);
    // This narrows the return to only the stocks that are most current.
    return new CursorLoader(
            this,
            QuoteProvider.Prices.historicRange(mStockName, pDate, cDate),
            Projections.HISTORIC,
            null,
            null,
            null
    );
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    Log.e(LOG_TAG, "Finishing loader");
    mCursorAdapter.swapCursor(data);
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) {
    Log.e(LOG_TAG, "Restarting loader");
    mCursorAdapter.swapCursor(null);
  }

  private void goLoader() {
    Utils.goLoader(this, CURSOR_LOADER_ID, this);
  }

  private void configChart(){
    linechart = (LineChartView) findViewById(R.id.lch_history);
    linechart.setAxisColor(getResources().getColor(R.color.colorSecondaryText));
    linechart.setLabelsColor(getResources().getColor(R.color.colorSecondaryText));
  }
}
