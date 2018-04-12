package com.keemsa.popularmovies.gcm;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * Created by sebastian on 11/7/16.
 * based on https://github.com/udacity/Advanced_Android_Development/blob/3e857821a6c8520e2f29c05e76ec8c36ad50dcb2/app/src/main/java/com/example/android/sunshine/app/gcm/MyInstanceIDListenerService.java
 */
public class MoviesIDListenerService extends InstanceIDListenerService {

    private static final String TAG = "MoviesIDLS";

    /*
     *  Called if InstanceID token is updated. This may occur if the security of
     *  the previous token had been compromised. This call is initiated by the
     *  InstanceID provider.
     */
    @Override
    public void onTokenRefresh() {
        Intent intent = new Intent(this, RegistrationIntentService.class);
        startService(intent);
    }
}
