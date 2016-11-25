package com.sam_chordas.android.stockhawk.ui;

import android.content.Context;
import android.support.v4.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
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
  private Handler mServiceHandler;
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
    configServiceHandler();
    goLoader();

    mServiceIntent = new Intent(this, StockIntentService.class);
    mServiceIntent.putExtra(StockIntentService.INVOKER_MESSENGER, new Messenger(mServiceHandler));
  }

  @Override
  public void onResume() {
    super.onResume();
    goLoader();
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

    if (id == R.id.action_change_units){
      // this is for changing stock changes from percent value to dollar value
      Utils.showPercent = !Utils.showPercent;
      this.getContentResolver().notifyChange(QuoteProvider.Quotes.CONTENT_URI, null);
    }

    return super.onOptionsItemSelected(item);
  }

  public Loader<Cursor> onCreateLoader(int id, Bundle args){
    // This narrows the return to only the stocks that are most current.
    return new CursorLoader(
            this,
            QuoteProvider.Quotes.CONTENT_URI,
            Projections.STOCK,
            QuoteColumns.ISCURRENT + " = ?",
            new String[]{"1"},
            null
    );
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data){
    mCursorAdapter.swapCursor(data);
    mCursor = data;
    if(data.getCount() == 0){
      saveAppStatus(AppStatus.STOCK_STATUS_UNKNOWN);
    }
    Utils.updateStockStatusView(this, txt_message);
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader){
    mCursorAdapter.swapCursor(null);
  }

  private void saveAppStatus(@AppStatus.StockStatus int status){
    Utils.setSharedPreference(
            this,
            getString(R.string.pref_key_stock_status),
            status,
            true
    );
  }

  private void configServiceHandler(){
    // As stated in http://stackoverflow.com/a/7871538/1065981
    mServiceHandler = new Handler() {
      @Override
      public void handleMessage(Message msg) {
        Bundle reply = msg.getData();
        String notification = reply.getString(StockIntentService.WORK_DONE);
        if (notification.equals(StockIntentService.WORK_DONE)) {
          goLoader();
        }
      }
    };
  }

  private void connectToServer(){
    // Run the initialize task service so that some stocks appear upon an empty database
    mServiceIntent.putExtra("tag", "init");
    if (isConnected){
        /*
           TODO: Why is the service started here?
           Once the loader has finished and no data have been found,
           the service should be started. If data is found, then the
           service should start to update the db
         */
      startService(mServiceIntent);
    } else{
      saveAppStatus(AppStatus.STOCK_STATUS_NO_CONNECTION);
      Utils.updateStockStatusView(this, txt_message);
    }
  }

  private void configRecycler(){
    recycler_view = (RecyclerView) findViewById(R.id.recycler_view);
    recycler_view.setLayoutManager(new LinearLayoutManager(this));
    recycler_view.addOnItemTouchListener(new RecyclerViewItemClickListener(this,
            new RecyclerViewItemClickListener.OnItemClickListener() {
              @Override public void onItemClick(View v, int position) {
                //TODO:
                // do something on item click
              }
            }));

    mCursorAdapter = new QuoteCursorAdapter(this, null);
    recycler_view.setAdapter(mCursorAdapter);

    ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mCursorAdapter);
    mItemTouchHelper = new ItemTouchHelper(callback);
    mItemTouchHelper.attachToRecyclerView(recycler_view);
  }

  private void configAddButton(){
    fab = (FloatingActionButton) findViewById(R.id.fab);
    fab.attachToRecyclerView(recycler_view);
    fab.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        if (isConnected){
          new MaterialDialog.Builder(mContext).title(R.string.symbol_search)
                  .content(R.string.content_test)
                  .inputType(InputType.TYPE_CLASS_TEXT)
                  .input(R.string.input_hint, R.string.input_prefill, new MaterialDialog.InputCallback() {
                    @Override public void onInput(MaterialDialog dialog, CharSequence input) {
                      // On FAB click, receive user input. Make sure the stock doesn't already exist
                      // in the DB and proceed accordingly
                      Cursor c = getContentResolver().query(
                              QuoteProvider.Quotes.CONTENT_URI,
                              Projections.STOCK,
                              QuoteColumns.SYMBOL + "= ?",
                              new String[] { input.toString() },
                              null
                      );
                      if (c.getCount() != 0) {
                        Toast toast = Toast.makeText(MyStocksActivity.this, "This stock is already saved!", Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, Gravity.CENTER, 0);
                        toast.show();
                        return;
                      } else {
                        // Add the stock to DB
                        mServiceIntent.putExtra("tag", "add");
                        mServiceIntent.putExtra("symbol", input.toString());

                        /*
                            Save name of the stock for further use
                         */
                        Utils.setSharedPreference(
                                mContext,
                                mContext.getString(R.string.pref_key_stock_queried),
                                input.toString(),
                                true);

                        startService(mServiceIntent);
                      }
                    }
                  })
                  .show();
        } else {
          saveAppStatus(AppStatus.STOCK_STATUS_NO_CONNECTION);
          Utils.updateStockStatusView(mContext, txt_message);
        }
      }
    });
  }

  private void configPeriodicTask(){
    if (isConnected){
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
}
