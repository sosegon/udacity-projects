package com.keemsa.popularmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by sebastian on 10/4/16.
 */
public class Utility {

    private static final String LOG_TAG = Utility.class.getSimpleName();

    public static long getDateInMilliSeconds(String date) {

        SimpleDateFormat format = new SimpleDateFormat("yyyy-mm-dd");
        format.setTimeZone(TimeZone.getTimeZone("GMT+0:01")); // one minute ahead
        try {
            Date argDate = format.parse(date);
            return argDate.getTime();
        } catch (ParseException e) {
            Log.e(LOG_TAG, "Error while converting date to milliseconds");
        }

        return -1;
    }

    public static String formatPosterUrl(String posterUrl) {
        if (posterUrl.charAt(0) == '/') {
            return posterUrl.substring(1);
        }
        return posterUrl;
    }

    public static String getPreferredQueryBy(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String queryBy = pref.getString(context.getString(R.string.prf_key_sort), context.getString(R.string.prf_default_sort));

        return queryBy;
    }

    public static Uri createTrailerUri(String site, String key) {
        if (site.toLowerCase().equals("youtube")) {
            return Uri.parse("http://www.youtube.com").buildUpon()
                    .appendPath("watch")
                    .appendQueryParameter("v", key).build();
        }
        else if (site.toLowerCase().equals("vimeo")) {
            return Uri.parse("http://www.vimeo.com").buildUpon()
                    .appendPath(key).build();
        }

        return null;
    }
}
