package com.keemsa.popularmovies;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by sebastian on 10/31/16.
 */
public interface AppStatus {
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({MOVIES_STATUS_OK, MOVIES_STATUS_SERVER_DOWN, MOVIES_STATUS_SERVER_INVALID, MOVIES_STATUS_UNKNOWN})
    @interface MoviesStatus {}

    int MOVIES_STATUS_OK = 0;
    int MOVIES_STATUS_SERVER_DOWN = 1;
    int MOVIES_STATUS_SERVER_INVALID = 2;
    int MOVIES_STATUS_UNKNOWN = 3;
}
