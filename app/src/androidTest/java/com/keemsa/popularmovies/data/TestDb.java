package com.keemsa.popularmovies.data;

import android.content.ContentValues;
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
        mContext.deleteDatabase(MovieDatabase.getInstance(mContext).getDatabaseName());
    }

    public void setUp() {
        deleteDatabase();
    }

    public void testCreateDb() throws Throwable {
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(com.keemsa.popularmovies.data.MovieDatabase.MOVIE);
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

        HashSet<String> trailer_columns = new HashSet<String>();
        trailer_columns.add(TrailerColumns._ID);
        trailer_columns.add(TrailerColumns.KEY);
        trailer_columns.add(TrailerColumns.SITE);
        trailer_columns.add(TrailerColumns.NAME);
        trailer_columns.add(TrailerColumns.TYPE);
        trailer_columns.add(TrailerColumns.MOVIE_ID);

        HashSet<String> review_columns = new HashSet<String>();
        review_columns.add(ReviewColumns._ID);
        review_columns.add(ReviewColumns.AUTHOR);
        review_columns.add(ReviewColumns.CONTENT);
        review_columns.add(ReviewColumns.URL);
        review_columns.add(ReviewColumns.MOVIE_ID);

        HashSet[] column_names = {movie_columns, trailer_columns, review_columns};

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

    public void testMovieTable() {
        setUp();

        SQLiteDatabase db = MovieDatabase.getInstance(mContext).getWritableDatabase();
        ContentValues contentMovie = TestUtilities.createMovieValues();

        // Insert ContentValues in db
        long id = db.insert(com.keemsa.popularmovies.data.MovieDatabase.MOVIE, null, contentMovie);

        assertTrue(id != -1); // Successfully inserted

        Cursor c = db.query(
                com.keemsa.popularmovies.data.MovieDatabase.MOVIE, // Table
                null, // columns
                MovieColumns._ID + " = ?", // selection
                new String[]{contentMovie.get(MovieColumns._ID).toString()}, // selection args
                null, // groupBy
                null, // having
                null, // orderBy
                null // limit
        );

        TestUtilities.validateCursor("", c, contentMovie);

        assertFalse("Error: more than one record returned from movie query", c.moveToNext());

        c.close();
        db.close();
    }

    public void testTrailerTable() {
        setUp();

        // Insert movie to make sure there is a row with the foreign key indicated by the trailer
        long id = insertMovie();
        assertTrue(id != -1);

        SQLiteDatabase db = MovieDatabase.getInstance(mContext).getWritableDatabase();
        // Insert ContentValues in db
        ContentValues contentTrailer = TestUtilities.createTrailerValues();
        id = db.insert(com.keemsa.popularmovies.data.MovieDatabase.TRAILER, null, contentTrailer);

        assertTrue(id != -1); // Successfully inserted
        Cursor c = db.query(
                com.keemsa.popularmovies.data.MovieDatabase.TRAILER, // Table
                null, // columns
                TrailerColumns._ID + " = ? and " + TrailerColumns.MOVIE_ID + " = ?", // selection
                new String[]{contentTrailer.getAsString(TrailerColumns._ID), contentTrailer.getAsString(TrailerColumns.MOVIE_ID)}, // selection args
                null, // groupBy
                null, // having
                null, // orderBy
                null // limit
        );

        TestUtilities.validateCursor("", c, contentTrailer);

        assertFalse("Error: more than one record returned from movie query", c.moveToNext());

        c.close();
        db.close();
    }

    public void testReviewTable() {
        setUp();

        // Insert movie to make sure there is a row with the foreign key indicated by the trailer
        long id = insertMovie();
        assertTrue(id != -1);

        SQLiteDatabase db = MovieDatabase.getInstance(mContext).getWritableDatabase();
        // Insert ContentValues in db
        ContentValues contentReview = TestUtilities.createReviewValues();
        id = db.insert(com.keemsa.popularmovies.data.MovieDatabase.REVIEW, null, contentReview);

        assertTrue(id != -1); // Successfully inserted

        Cursor c = db.query(
                com.keemsa.popularmovies.data.MovieDatabase.REVIEW, // Table
                null, // columns
                ReviewColumns._ID + " = ? and " + ReviewColumns.MOVIE_ID + " = ?", // selection
                new String[]{contentReview.get(ReviewColumns._ID).toString(), contentReview.get(ReviewColumns.MOVIE_ID).toString()}, // selection args
                null, // groupBy
                null, // having
                null, // orderBy
                null // limit
        );

        TestUtilities.validateCursor("", c, contentReview);

        assertFalse("Error: more than one record returned from movie query", c.moveToNext());

        c.close();
        db.close();
    }

    public long insertMovie() {
        SQLiteDatabase db = MovieDatabase.getInstance(mContext).getWritableDatabase();
        ContentValues movieValues = TestUtilities.createMovieValues();
        long id = db.insert(com.keemsa.popularmovies.data.MovieDatabase.MOVIE, null, movieValues);
        db.close();
        return id;
    }
}
