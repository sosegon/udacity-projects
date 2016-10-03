package com.keemsa.popularmovies.data;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

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

        setUp();

        SQLiteDatabase db = MovieDatabase.getInstance(mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // Check tables were created
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        assertTrue("Error: This means that the database has not been created correctly", c.moveToFirst());

        // verify that the tables have been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while (c.moveToNext());

        // if this fails, it means that the database doesn't contain movie table
        assertTrue("Error: Your database was created without movie entry table", tableNameHashSet.isEmpty());

        // Check correct tables were created
        c = db.rawQuery("PRAGMA table_info(" + com.keemsa.popularmovies.data.MovieDatabase.MOVIE + ")", null);
        assertTrue("Error: This means that we were unable to query the database for table information.", c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> movieColumnHashSet = new HashSet<String>();
        movieColumnHashSet.add(MovieColums._ID);
        movieColumnHashSet.add(MovieColums.TITLE);
        movieColumnHashSet.add(MovieColums.SYNOPSIS);
        movieColumnHashSet.add(MovieColums.POSTER_URL);
        movieColumnHashSet.add(MovieColums.RELEASE_DATE);
        movieColumnHashSet.add(MovieColums.RATING);


        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            movieColumnHashSet.remove(columnName);
        } while (c.moveToNext());

        // if this fails, it means that the database doesn't contain all of the required movie entry columns
        assertTrue("Error: The database doesn't contain all of the required location entry columns", movieColumnHashSet.isEmpty());
        db.close();
    }
}
