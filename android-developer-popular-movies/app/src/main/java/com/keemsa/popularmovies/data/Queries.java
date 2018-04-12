package com.keemsa.popularmovies.data;

/**
 * Created by sebastian on 11/11/16.
 */
public interface Queries {

    String[] MOVIE_PROJECTION = {
        MovieColumns._ID,
        MovieColumns.TITLE,
        MovieColumns.SYNOPSIS,
        MovieColumns.POSTER_URL,
        MovieColumns.QUERY_TYPE,
        MovieColumns.RELEASE_DATE,
        MovieColumns.RATING
    };

    int MOVIE_ID = 0,
        MOVIE_TITLE = 1,
        MOVIE_SYNOPSIS = 2,
        MOVIE_POSTER_URL = 3,
        MOVIE_QUERY_TYPE = 4,
        MOVIE_RELEASE_DATE = 5,
        MOVIE_RATING = 6;

    String[] TRAILER_PROJECTION = {
            TrailerColumns._ID,
            TrailerColumns.KEY,
            TrailerColumns.NAME,
            TrailerColumns.SITE
    };

    int TRAILER_ID = 0,
        TRAILER_KEY = 1,
        TRAILER_NAME = 2,
        TRAILER_SITE = 3;

    String[] REVIEW_PROJECTION = {
        ReviewColumns.AUTHOR,
        ReviewColumns.CONTENT
    };

    int REVIEW_AUTHOR = 0,
        REVIEW_CONTENT = 1;
}

