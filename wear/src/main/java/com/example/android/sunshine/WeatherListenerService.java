package com.example.android.sunshine;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Created by sebastian on 2/21/17.
 */

public class WeatherListenerService extends WearableListenerService {

  private final static String LOG_TAG = WeatherListenerService.class.getSimpleName();
  private GoogleApiClient mGoogleApiClient;

  @Override
  public void onCreate() {
    super.onCreate();
    mGoogleApiClient = new GoogleApiClient.Builder(this)
            .addApi(Wearable.API)
            .build();
    mGoogleApiClient.connect();
  }

  @Override
  public void onDataChanged(DataEventBuffer dataEvents) {
    Log.e(LOG_TAG, "onDataChanged: " + dataEvents);

    for(DataEvent dataEvent : dataEvents){
      if(dataEvent.getType() == DataEvent.TYPE_CHANGED){
        DataMap dataMap = DataMapItem.fromDataItem(dataEvent.getDataItem()).getDataMap();
        String path = dataEvent.getDataItem().getUri().getPath();
        if(path.equals("/weather-data")){
          int maxTemp = dataMap.getInt("maxTemp");
          int minTemp = dataMap.getInt("minTemp");

          Intent intent = new Intent();
          intent.setAction(Intent.ACTION_ATTACH_DATA);
          intent.putExtra("minTemp", minTemp);
          intent.putExtra("maxTemp", maxTemp);
          sendBroadcast(intent);
        }
      }
    }

  }
}
