package com.keemsa.popularmovies;

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
        }
        catch (ParseException e ){
            Log.e(LOG_TAG, "Error while converting date to milliseconds");
        }

        return -1;
    }
}
