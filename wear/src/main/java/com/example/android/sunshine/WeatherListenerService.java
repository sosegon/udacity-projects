package com.example.android.sunshine;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.InputStream;

/**
 * Created by sebastian on 2/21/17.
 */

public class WeatherListenerService extends WearableListenerService implements LoadBitmapFinalizer {

  private final static String LOG_TAG = WeatherListenerService.class.getSimpleName();
  private GoogleApiClient mGoogleApiClient;
  private int minTemp = 0, maxTemp = 0;

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
          maxTemp = dataMap.getInt("maxTemp");
          minTemp = dataMap.getInt("minTemp");
          Asset iconAsset = dataMap.getAsset("icon");
          new LoadBitmapAsyncTask(this).execute(iconAsset);
        }
      }
    }
  }

  @Override
  public void processBitmap(Bitmap bitmap) {

    Intent intent = new Intent();
    intent.setAction(Intent.ACTION_ATTACH_DATA);
    intent.putExtra("minTemp", minTemp);
    intent.putExtra("maxTemp", maxTemp);
    intent.putExtra("icon", scaleDown(bitmap, 60, true));

    sendBroadcast(intent);
  }

  // from http://stackoverflow.com/a/8471294/1065981
  private static Bitmap scaleDown(Bitmap realImage, float maxImageSize,
                                 boolean filter) {
    float ratio = Math.min(
            (float) maxImageSize / realImage.getWidth(),
            (float) maxImageSize / realImage.getHeight());
    int width = Math.round((float) ratio * realImage.getWidth());
    int height = Math.round((float) ratio * realImage.getHeight());

    Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width,
            height, filter);
    return newBitmap;
  }

  /*
   * Extracts {@link android.graphics.Bitmap} data from the
   * {@link com.google.android.gms.wearable.Asset}
   */
  private class LoadBitmapAsyncTask extends AsyncTask<Asset, Void, Bitmap> {

    private LoadBitmapFinalizer mFinalizer;

    public LoadBitmapAsyncTask(LoadBitmapFinalizer finalizer){
      mFinalizer = finalizer;
    }

    @Override
    protected Bitmap doInBackground(Asset... params) {

      if (params.length > 0) {

        Asset asset = params[0];

        InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                mGoogleApiClient, asset).await().getInputStream();

        if (assetInputStream == null) {
          Log.w(LOG_TAG, "Requested an unknown Asset.");
          return null;
        }
        return BitmapFactory.decodeStream(assetInputStream);

      } else {
        Log.e(LOG_TAG, "Asset must be non-null");
        return null;
      }
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
      if (bitmap != null) {
        Log.e(LOG_TAG, "asset decoded");
        mFinalizer.processBitmap(bitmap);
      }
    }
  }
}
