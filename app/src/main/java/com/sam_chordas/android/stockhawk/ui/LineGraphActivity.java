package com.sam_chordas.android.stockhawk.ui;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayout;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.db.chart.view.LineChartView;
import com.sam_chordas.android.stockhawk.AppStatus;
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
  private ProgressBar pgr_line;
  private GridLayout grl_line;
  private TextView txt_line;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_line_graph);

    configViews();

    // Unlike a recycler view where the adapter is associated to the view,
    // Here the situation is the opposite, the view is associated to the adapter
    final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) this.findViewById(android.R.id.content)).getChildAt(0);
    mCursorAdapter = new HistoricPriceCursorAdapter(this, viewGroup, null);

    mServiceIntent = new Intent(this, StockIntentService.class);
    if(getIntent() != null){
      mStockName = getIntent().getStringExtra("stockName");

      goLoader();

//      This is the trick: The loader is restarted every time a change in the dataset
//      happens. When the application first starts, it gets the data from the
//      content provider. However, that data may be not the most recent, or
//      there may not be data at all. So the server is queried, when this tasks
//      finishes, new data may have been collected or not. In either case the
//      activity needs to get notified to update the ui properly. The special
//      case happens when there is no data in the content provider, and no
//      data were retrieved from the server. In this case, the data from
//      the content provider has not been modified, hence the loader is
//      not restarted. To allow the loader to be restared, no matter the
//      case, a dumb record is added at the beginning of the server
//      transaction, and it is deleted at the end. This solves the problem
//      of the ui being updated properly. However, the activity needs to
//      know if the transaction to the server is still in progress, that's
//      why the shared preference "querying_historic" is used.
      Utils.setSharedPreference(this, getString(R.string.pref_key_querying_historic), 1, true);

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
    int queryingHistoric = Utils.getQueryingHistoric(this);
    if(data.getCount() == 0 && queryingHistoric == 1){ // Querying the server
      pgr_line.setVisibility(View.VISIBLE);
      txt_line.setVisibility(View.GONE);
      grl_line.setVisibility(View.GONE);
    } else if (data.getCount() == 0 && queryingHistoric == 0){ // Finished querying the server
      Utils.updateStockStatusView(this, txt_line, false);
      saveAppStatus(AppStatus.STOCK_STATUS_NO_DATA);
      pgr_line.setVisibility(View.GONE);
      txt_line.setVisibility(View.VISIBLE);
      grl_line.setVisibility(View.GONE);
      Utils.updateStockStatusView(this, txt_line, false);
    } else if (data.getCount() > 0){ // Data from the loader or updated from the server
      pgr_line.setVisibility(View.GONE);
      txt_line.setVisibility(View.GONE);
      grl_line.setVisibility(View.VISIBLE);
      Utils.updateStockStatusView(this, txt_line, true);
    }
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) {
    Log.e(LOG_TAG, "Restarting loader");
    mCursorAdapter.swapCursor(null);
  }

  private void goLoader() {
    Utils.goLoader(this, CURSOR_LOADER_ID, this);
  }

  private void configViews(){
    linechart = (LineChartView) findViewById(R.id.lch_history);
    linechart.setAxisColor(getResources().getColor(R.color.colorSecondaryText));
    linechart.setLabelsColor(getResources().getColor(R.color.colorSecondaryText));

    pgr_line = (ProgressBar) findViewById(R.id.prg_line);
    grl_line = (GridLayout) findViewById(R.id.grl_line);
    txt_line = (TextView) findViewById(R.id.txt_line);
  }

  private void saveAppStatus(@AppStatus.StockStatus int status) {
    Utils.setSharedPreference(
            this,
            getString(R.string.pref_key_stock_status),
            status,
            false
    );
  }
}
