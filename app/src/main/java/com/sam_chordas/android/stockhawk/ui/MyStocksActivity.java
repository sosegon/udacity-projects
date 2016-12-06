package com.sam_chordas.android.stockhawk.ui;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.CursorLoader;
import android.database.Cursor;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.sam_chordas.android.stockhawk.AppStatus;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.Projections;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.QuoteCursorAdapter;
import com.sam_chordas.android.stockhawk.rest.RecyclerViewItemClickListener;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.service.ContentProviderReceiver;
import com.sam_chordas.android.stockhawk.service.ContentProviderService;
import com.sam_chordas.android.stockhawk.service.StockIntentService;
import com.sam_chordas.android.stockhawk.service.StockTaskService;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;
import com.melnykov.fab.FloatingActionButton;
import com.sam_chordas.android.stockhawk.touch_helper.SimpleItemTouchHelperCallback;

public class MyStocksActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

  /**
   * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
   */

  /**
   * Used to store the last screen title. For use in {@link #restoreActionBar()}.
   */
  private CharSequence mTitle;
  private Intent mServiceIntent;
  private ItemTouchHelper mItemTouchHelper;
  private static final int CURSOR_LOADER_ID = 0;
  private QuoteCursorAdapter mCursorAdapter;
  private Context mContext;
  private Cursor mCursor;
  boolean isConnected;

  private TextView txt_message;
  private RecyclerView recycler_view;
  private FloatingActionButton fab;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_my_stocks);

    mContext = this;
    mTitle = getTitle();
    isConnected = Utils.isNetworkAvailable(mContext);
    txt_message = (TextView) findViewById(R.id.txt_status);

    configRecycler();
    configAddButton();
    configPeriodicTask();
    goLoader();
    updateStocks();

    mServiceIntent = new Intent(this, StockIntentService.class);
  }

  @Override
  public void onResume() {
    super.onResume();
    goLoader();
    saveAppStatus(AppStatus.STOCK_STATUS_OK); // reset
  }

  public void restoreActionBar() {
    ActionBar actionBar = getSupportActionBar();
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
    actionBar.setDisplayShowTitleEnabled(true);
    actionBar.setTitle(mTitle);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.my_stocks, menu);
    restoreActionBar();
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    if (id == R.id.action_change_units) {
      // this is for changing stock changes from percent value to dollar value
      Utils.showPercent = !Utils.showPercent;
      this.getContentResolver().notifyChange(QuoteProvider.Quotes.CONTENT_URI, null);
    }

    return super.onOptionsItemSelected(item);
  }

  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    // This narrows the return to only the stocks that are most current.
    return new CursorLoader(
            this,
            QuoteProvider.Quotes.CONTENT_URI,
            Projections.STOCK,
            null,
            null,
            null
    );
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    mCursorAdapter.swapCursor(data);
    mCursor = data;
    if (data.getCount() == 0) {
      saveAppStatus(AppStatus.STOCK_STATUS_NO_STOCKS);
    }
    Utils.updateStockStatusView(this, txt_message, mCursor.getCount() > 0);
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) {
    mCursorAdapter.swapCursor(null);
  }

  private void saveAppStatus(@AppStatus.StockStatus int status) {
    Utils.setSharedPreference(
            this,
            getString(R.string.pref_key_stock_status),
            status,
            true
    );
  }

  private void configRecycler() {
    recycler_view = (RecyclerView) findViewById(R.id.recycler_view);
    recycler_view.setLayoutManager(new LinearLayoutManager(this));
    recycler_view.addOnItemTouchListener(new RecyclerViewItemClickListener(this,
            new RecyclerViewItemClickListener.OnItemClickListener() {
              @Override
              public void onItemClick(View v, int position) {
                mCursor.moveToPosition(position);
                String stockName = mCursor.getString(Projections.STOCK_SYMBOL);
                Intent intent = new Intent(MyStocksActivity.this, LineGraphActivity.class);
                intent.putExtra("stockName", stockName);
                startActivity(intent);
              }
            }));

    mCursorAdapter = new QuoteCursorAdapter(this, null);
    recycler_view.setAdapter(mCursorAdapter);

    ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mCursorAdapter);
    mItemTouchHelper = new ItemTouchHelper(callback);
    mItemTouchHelper.attachToRecyclerView(recycler_view);
  }

  private void configAddButton() {
    fab = (FloatingActionButton) findViewById(R.id.fab);
    fab.attachToRecyclerView(recycler_view);
    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (isConnected) {
          new MaterialDialog.Builder(mContext).title(R.string.symbol_search)
                  .content(R.string.content_test)
                  .inputType(InputType.TYPE_CLASS_TEXT)
                  .input(R.string.input_hint, R.string.input_prefill, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                      if(!Utils.isNetworkAvailable(mContext)){
                        Toast toast = Toast.makeText(MyStocksActivity.this,mContext.getString(R.string.sta_no_connection), Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, Gravity.CENTER, 0);
                        toast.show();
                        return;
                      }
                      // On FAB click, receive user input. Make sure the stock doesn't already exist
                      // in the DB and proceed accordingly
                      String symbol = input.toString().toUpperCase();
                      Cursor c = getContentResolver().query(
                              QuoteProvider.Quotes.CONTENT_URI,
                              Projections.STOCK,
                              QuoteColumns.SYMBOL + "= ?",
                              new String[]{symbol},
                              null
                      );
                      if (c.getCount() != 0) {
                        Toast toast = Toast.makeText(MyStocksActivity.this, "This stock is already saved!", Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, Gravity.CENTER, 0);
                        toast.show();
                        return;
                      } else {

                        // Add new record to the content provider
                        // This record will be temporary just to display
                        // an item in the ui, once the service finishes its
                        // work, the record will be either updated (if the
                        // server was successful) or deleted (if the server
                        // failed)
                        ContentValues cv = new ContentValues();
                        cv.put(QuoteColumns.SYMBOL, symbol);
                        cv.put(QuoteColumns.PERCENT_CHANGE, 0);
                        cv.put(QuoteColumns.CHANGE, 0);
                        cv.put(QuoteColumns.BIDPRICE, 0);
                        cv.put(QuoteColumns.ISUP, 0);
                        cv.put(QuoteColumns.ISCURRENT, 1); //
                        cv.put(QuoteColumns.ISTEMP, 1);

                        // inser right away, the service will update or delete the record accordingly
                        mContext.getContentResolver().insert(
                                QuoteProvider.Quotes.CONTENT_URI,
                                cv
                        );

                        // Add the stock to DB
                        mServiceIntent.putExtra("tag", "add");
                        mServiceIntent.putExtra("symbol", symbol);

                        // Save name of the stock for further use
                        Utils.setSharedPreference(
                                mContext,
                                mContext.getString(R.string.pref_key_stock_queried),
                                symbol,
                                true);

                        startService(mServiceIntent);
                      }
                    }
                  })
                  .show();
        } else {
          saveAppStatus(AppStatus.STOCK_STATUS_NO_CONNECTION);
          Utils.updateStockStatusView(mContext, txt_message, false);
        }
      }
    });
  }

  private void configPeriodicTask() {
    if (isConnected) {
      long period = 3600L;
      long flex = 10L;
      String periodicTag = "periodic";

      // create a periodic task to pull stocks once every hour after the app has been opened. This
      // is so Widget data stays up to date.
      PeriodicTask periodicTask = new PeriodicTask.Builder()
              .setService(StockTaskService.class)
              .setPeriod(period)
              .setFlex(flex)
              .setTag(periodicTag)
              .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
              .setRequiresCharging(false)
              .build();
      // Schedule task with tag "periodic." This ensure that only the stocks present in the DB
      // are updated.
      GcmNetworkManager.getInstance(mContext).schedule(periodicTask);
    }
  }

  private void goLoader() {
    Utils.goLoader(this, CURSOR_LOADER_ID, this);
  }

  private void updateStocks(){
    Intent contentProviderIntent = new Intent(this, ContentProviderService.class);
    contentProviderIntent.putExtra(
            ContentProviderService.CP_SERVICE_OPERATION,
            ContentProviderService.CP_SERVICE_UPDATE_TO_QUERY_SERVER
    );

    startService(contentProviderIntent);
  }
}
