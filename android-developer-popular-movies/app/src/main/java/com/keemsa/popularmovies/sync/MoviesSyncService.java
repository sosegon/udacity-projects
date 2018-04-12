package com.keemsa.popularmovies.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by sebastian on 10/26/16.
 */
public class MoviesSyncService extends Service {

    private static final String LOG_TAG = MoviesSyncService.class.getSimpleName();
    private static MoviesSyncAdapter mSyncAdapter;
    private static final Object mSyncAdapterLock = new Object();

    @Override
    public void onCreate() {
        Log.d(LOG_TAG, "onCreate called");
        synchronized (mSyncAdapterLock) {
            if (mSyncAdapter == null) {
                mSyncAdapter = new MoviesSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mSyncAdapter.getSyncAdapterBinder();
    }
}
