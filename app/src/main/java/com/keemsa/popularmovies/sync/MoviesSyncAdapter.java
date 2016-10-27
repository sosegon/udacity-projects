package com.keemsa.popularmovies.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by sebastian on 10/26/16.
 */
public class MoviesSyncAdapter extends AbstractThreadedSyncAdapter {

    private final String LOG_TAG = MoviesSyncAdapter.class.getSimpleName();
    ContentResolver mContentResolver;
    private int response;

    public MoviesSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContentResolver = context.getContentResolver();
    }

    @Override
    public void onPerformSync(Account account, Bundle bundle, String s, ContentProviderClient contentProviderClient, SyncResult syncResult) {
        Log.d(LOG_TAG, "onPerformSync Called");
    }
}
