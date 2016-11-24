package com.sam_chordas.android.stockhawk.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.TaskParams;
import com.sam_chordas.android.stockhawk.AppStatus;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.rest.Utils;

/**
 * Created by sam_chordas on 10/1/15.
 */
public class StockIntentService extends IntentService {

  public final static String LOG_TAG = StockIntentService.class.getSimpleName();
  public static final String INVOKER_MESSENGER = "ims";
  public static final String WORK_DONE = "wdn";

  public StockIntentService(){
    super(StockIntentService.class.getName());
  }

  public StockIntentService(String name) {
    super(name);
  }

  @Override protected void onHandleIntent(Intent intent) {
    Log.d(StockIntentService.class.getSimpleName(), "Stock Intent Service");
    StockTaskService stockTaskService = new StockTaskService(this);
    Bundle args = new Bundle();
    if (intent.getStringExtra("tag").equals("add")){
      args.putString("symbol", intent.getStringExtra("symbol"));
    }
    // We can call OnRunTask from the intent service to force it to run immediately instead of
    // scheduling a task.
    int result = stockTaskService.onRunTask(new TaskParams(intent.getStringExtra("tag"), args));
    if(result == GcmNetworkManager.RESULT_SUCCESS){
      Utils.setSharedPreference(
              this,
              this.getString(R.string.pref_key_stock_status),
              AppStatus.STOCK_STATUS_OK,
              false
      );
    }
    contactInvoker(intent);
  }

  // As stated in http://stackoverflow.com/a/7871538/1065981
  private void contactInvoker(Intent intent) {
    Messenger messenger = (Messenger) intent.getExtras().get(INVOKER_MESSENGER);
    Message msg = Message.obtain();
    Bundle data = new Bundle();
    data.putString(WORK_DONE, WORK_DONE);
    msg.setData(data);
    try {
      messenger.send(msg);
    } catch (RemoteException e){
      Log.e(LOG_TAG, e.getMessage());
    }
  }
}
