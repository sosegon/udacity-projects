package com.keemsa.popularmovies.data;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import com.keemsa.popularmovies.provider.MovieDatabase;

import java.util.HashSet;

/**
 * Created by sebastian on 10/2/16.
 */
public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    void deleteDatabase() {
        mContext.deleteDatabase(MovieDatabase.class.getSimpleName());
    }

    public void setUp() {
        deleteDatabase();
    }

    public void testCreateDb() throws Throwable {
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(com.keemsa.popularmovies.data.MovieDatabase.MOVIE);
        tableNameHashSet.add(com.keemsa.popularmovies.data.MovieDatabase.FAV_MOVIE);
        tableNameHashSet.add(com.keemsa.popularmovies.data.MovieDatabase.TRAILER);
        tableNameHashSet.add(com.keemsa.popularmovies.data.MovieDatabase.REVIEW);

        setUp();

        SQLiteDatabase db = MovieDatabase.getInstance(mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // Check if tables were created
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        assertTrue("Error: The database has not been created correctly", c.moveToFirst());

        // verify that the tables have been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while (c.moveToNext());

        // if this fails, it means that the database doesn't contain movie table
        assertTrue("Error: Database created without all the tables", tableNameHashSet.isEmpty());

        // Check correct tables were created
        String[] tables_names = {
                com.keemsa.popularmovies.data.MovieDatabase.MOVIE,
                com.keemsa.popularmovies.data.MovieDatabase.FAV_MOVIE,
                com.keemsa.popularmovies.data.MovieDatabase.TRAILER,
                com.keemsa.popularmovies.data.MovieDatabase.REVIEW
        };

        HashSet<String> movie_columns = new HashSet<String>();
        movie_columns.add(MovieColumns._ID);
        movie_columns.add(MovieColumns.TITLE);
        movie_columns.add(MovieColumns.SYNOPSIS);
        movie_columns.add(MovieColumns.POSTER_URL);
        movie_columns.add(MovieColumns.RELEASE_DATE);
        movie_columns.add(MovieColumns.RATING);

        HashSet<String> fav_movie_columns = new HashSet<String>();
        fav_movie_columns.add(FavMovieColumns._ID);
        fav_movie_columns.add(FavMovieColumns.TITLE);
        fav_movie_columns.add(FavMovieColumns.SYNOPSIS);
        fav_movie_columns.add(FavMovieColumns.POSTER_URL);
        fav_movie_columns.add(FavMovieColumns.RELEASE_DATE);
        fav_movie_columns.add(FavMovieColumns.RATING);

        HashSet<String> trailer_columns = new HashSet<String>();
        trailer_columns.add(TrailerColumns._ID);
        trailer_columns.add(TrailerColumns.KEY);
        trailer_columns.add(TrailerColumns.SITE);
        trailer_columns.add(TrailerColumns.MOVIE_ID);

        HashSet<String> review_columns = new HashSet<String>();
        review_columns.add(ReviewColumns._ID);
        review_columns.add(ReviewColumns.AUTHOR);
        review_columns.add(ReviewColumns.CONTENT);
        review_columns.add(ReviewColumns.URL);
        review_columns.add(ReviewColumns.MOVIE_ID);

        HashSet[] column_names = {movie_columns, fav_movie_columns, trailer_columns, review_columns};

        for (int i = 0; i < tables_names.length; i++) {
            c = db.rawQuery("PRAGMA table_info(" + tables_names[i] + ")", null);
            assertTrue("Error: Unable to query the database for table information.", c.moveToFirst());

            int columnNameIndex = c.getColumnIndex("name");
            do {
                String columnName = c.getString(columnNameIndex);
                column_names[i].remove(columnName);
            } while (c.moveToNext());

            // if this fails, it means that the database doesn't contain all of the required movie entry columns
            assertTrue("Error: The database doesn't contain all of the required columns", column_names[i].isEmpty());
        }

        db.close();
    }
}
