package com.keemsa.popularmovies.gcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.keemsa.popularmovies.CatalogActivity;
import com.keemsa.popularmovies.R;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by sebastian on 11/7/16.
 * based on https://github.com/udacity/Advanced_Android_Development/blob/c08b7069488b0dbf025e3f2aec00217d4ed6ea4b/app/src/main/java/com/example/android/sunshine/app/gcm/MyGcmListenerService.java
 */
public class MoviesListenerService extends GcmListenerService {

    private static final String LOG_TAG = MoviesListenerService.class.getSimpleName();

    private static final String EXTRA_DATA = "data";
    private static final String EXTRA_MOVIE = "movie";
    public static final int NOTIFICATION_ID = 1;

    @Override
    public void onMessageReceived(String s, Bundle bundle) {
        if(!bundle.isEmpty()) {
            String gcmSenderId = getString(R.string.gcm_defaultSenderId);
            if(gcmSenderId.equals(s)) {
                try {
                    JSONObject jsonObject = new JSONObject(bundle.getString(EXTRA_DATA));
                    String movie = jsonObject.getString(EXTRA_MOVIE);
                    String alert = String.format(getString(R.string.gcm_movie_alert), movie);
                    sendNotification(alert);

                } catch (JSONException e) {

                }
                Log.i(LOG_TAG, "Received: " + bundle.toString());
            }

        }
    }

    /**
     *  Put the message into a notification and post it.
     *  This is just one simple example of what you might choose to do with a GCM message.
     *
     * @param message The alert message to be posted.
     */
    private void sendNotification(String message) {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, CatalogActivity.class), 0);

        // Notifications using both a large and a small icon (which yours should!) need the large
        // icon as a bitmap. So we need to create that here from the resource ID, and pass the
        // object along in our notification builder. Generally, you want to use the app icon as the
        // small icon, so that users understand what app is triggering this notification.
        Bitmap largeIcon = BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setLargeIcon(largeIcon)
                        .setContentTitle("Movie Alert!")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                        .setContentText(message)
                        .setPriority(NotificationCompat.PRIORITY_HIGH);
        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
