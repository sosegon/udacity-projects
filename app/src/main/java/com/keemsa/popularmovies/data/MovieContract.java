package com.keemsa.popularmovies.data;

import android.provider.BaseColumns;

/**
 * Created by sebastian on 9/30/16.
 */
public class MovieContract {
    public static final class MovieEntry implements BaseColumns {
        public static final String TABLE_NAME = "movie";

        // Text
        public static final String COlUMN_TITLE = "title";

        // Text
        public static final String COLUMN_SYNOPSIS = "synopsis";

        // Text
        public static final String COLUMN_POSTER_URL = "poster_url";

        // Integer? It may have to contain values in milliseconds to ease the filtering
        public static final String COLUMN_RELEASE_DATE = "release_date";

        // Real
        public static final String COLUMN_RATING = "rating";
    }
}
