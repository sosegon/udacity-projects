package com.keemsa.popularmovies.data;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import android.test.AndroidTestCase;

import java.util.ArrayList;

/**
 * Created by sebastian on 10/5/16.
 */
public class TestProvider extends AndroidTestCase {

    public static final String LOG_TAG = TestProvider.class.getSimpleName();

    public void deleteAllRecordsFromProvider() {
        mContext.getContentResolver().delete(
                MovieProvider.Movie.ALL,
                null,
                null
        );

        Cursor c = mContext.getContentResolver().query(
                MovieProvider.Movie.ALL,
                null,
                null,
                null,
                null
        );

        assertTrue("Error: Movie table has some previous records", c.getCount() == 0);
        c.close();

        mContext.getContentResolver().delete(
                MovieProvider.Trailer.ALL,
                null,
                null
        );

        c = mContext.getContentResolver().query(
                MovieProvider.Trailer.ALL,
                null,
                null,
                null,
                null
        );

        assertTrue("Error: Trailer table has some previous records", c.getCount() == 0);
        c.close();

        mContext.getContentResolver().delete(
                MovieProvider.Review.ALL,
                null,
                null
        );

        c = mContext.getContentResolver().query(
                MovieProvider.Review.ALL,
                null,
                null,
                null,
                null
        );

        assertTrue("Error: Review table has some previous records", c.getCount() == 0);
        c.close();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteAllRecordsFromProvider();
    }

    public void testProviderRegistry() {
        PackageManager pm = mContext.getPackageManager();

        // We define the component name based on the package name from the context and the
        // MovieProvider class.
        ComponentName componentName = new ComponentName(
                mContext.getPackageName(),
                com.keemsa.popularmovies.provider.MovieProvider.class.getName());
        try {
            // Fetch the provider info using the component name from the PackageManager
            // This throws an exception if the provider isn't registered.
            ProviderInfo pi = pm.getProviderInfo(componentName, 0);

            // Make sure that the registered authority matches the authority
            assertEquals(
                    "Error: MovieProvider registered with authority: " + pi.authority +
                            " instead of authority: " + MovieProvider.AUTHORITY,
                    pi.authority,
                    MovieProvider.AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            // The provider isn't registered correctly.
            assertTrue("Error: MovieProvider not registered at " + mContext.getPackageName(), false);
        }
    }

    public void testGetType() {
        // content://com.keemsa.popularmovies/movie
        String type = mContext.getContentResolver().getType(MovieProvider.Movie.ALL);
        assertEquals("Error: Type for " + MovieProvider.Movie.ALL + " is wrong", MovieProvider.Movie.CONTENT_DIR_TYPE, type);

        // content://com.keemsa.popularmovies/movie/#
        type = mContext.getContentResolver().getType(MovieProvider.Movie.withId(123));
        assertEquals("Error: Type for " + MovieProvider.Movie.withId(123) + "/# is wrong", MovieProvider.Movie.CONTENT_ITEM_TYPE, type);

        // content://com.keemsa.popularmovies/trailer
        type = mContext.getContentResolver().getType(MovieProvider.Trailer.ALL);
        assertEquals("Error: Type for " + MovieProvider.Trailer.ALL + " is wrong", MovieProvider.Trailer.CONTENT_DIR_TYPE, type);

        // content://com.keemsa.popularmovies/movie/#/trailer
        type = mContext.getContentResolver().getType(MovieProvider.Trailer.ofMovie(123));
        assertEquals("Error: Type for " + MovieProvider.Trailer.ofMovie(123) + " is wrong", MovieProvider.Trailer.CONTENT_DIR_TYPE, type);

        // content://com.keemsa.popularmovies/trailer/*
        type = mContext.getContentResolver().getType(MovieProvider.Trailer.withId("wexc"));
        assertEquals("Error: Type for " + MovieProvider.Trailer.withId("wexc") + " is wrong", MovieProvider.Trailer.CONTENT_ITEM_TYPE, type);

        // content://com.keemsa.popularmovies/review
        type = mContext.getContentResolver().getType(MovieProvider.Review.ALL);
        assertEquals("Error: Type for " + MovieProvider.Review.ALL + " is wrong", MovieProvider.Review.CONTENT_DIR_TYPE, type);

        // content://com.keemsa.popularmovies/movie/#/review
        type = mContext.getContentResolver().getType(MovieProvider.Review.ofMovie(123));
        assertEquals("Error: Type for " + MovieProvider.Review.ofMovie(123) + " is wrong", MovieProvider.Review.CONTENT_DIR_TYPE, type);

        // content://com.keemsa.popularmovies/review/*
        type = mContext.getContentResolver().getType(MovieProvider.Review.withId("wexc"));
        assertEquals("Error: Type for " + MovieProvider.Review.withId("wexc") + " is wrong", MovieProvider.Review.CONTENT_ITEM_TYPE, type);
    }

    public void testQueryMovie() {
        setupProvider();

        ContentValues movieValue = TestUtilities.createMovieValues();

        mContext.getContentResolver().insert(
                MovieProvider.Movie.ALL,
                movieValue
        );

        Cursor c = mContext.getContentResolver().query(
                MovieProvider.Movie.ALL,
                null,
                null,
                null,
                null
        );

        assertTrue(c.getCount() != 0);

        TestUtilities.validateCursor("", c, movieValue);
    }

    public void testUpdateMovie() {
        setupProvider();

        ContentValues movieValue = TestUtilities.createMovieValues();
        Uri movieUri = mContext.getContentResolver().insert(
                MovieProvider.Movie.ALL,
                movieValue
        );

        long movieRowId = ContentUris.parseId(movieUri);
        assertTrue(movieRowId != -1);

        ContentValues updatedMovieValue = new ContentValues(movieValue);
        updatedMovieValue.put(MovieColumns.RATING, 9.99);

        Cursor c = mContext.getContentResolver().query(
                MovieProvider.Movie.ALL,
                null,
                null,
                null,
                null
        );

        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        c.registerContentObserver(tco); // the provider has to notify the content observer about the changes

        int count = mContext.getContentResolver().update(
                MovieProvider.Movie.ALL,
                updatedMovieValue,
                MovieColumns._ID + "= ?",
                new String[]{movieValue.getAsString(MovieColumns._ID)}
        );

        assertTrue(count == 1);

        // Test to make sure our observer is called.  If not, throw an assertion.
        tco.waitForNotificationOrFail();
        c.unregisterContentObserver(tco);
        c.close();

        c = mContext.getContentResolver().query(
                MovieProvider.Movie.ALL,
                null,
                MovieColumns._ID + "= ?",
                new String[]{movieValue.getAsString(MovieColumns._ID)},
                null
        );

        TestUtilities.validateCursor("", c, updatedMovieValue);
    }

    public void testInsertMovie() {
        setupProvider();

        ContentValues movieValues = TestUtilities.createMovieValues();

        // Register an observer for the insert
        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(MovieProvider.Movie.ALL, true, tco);

        Uri movieUri = mContext.getContentResolver().insert(
                MovieProvider.Movie.ALL,
                movieValues
        );

        // Test to make sure our observer is called.  If not, throw an assertion.
        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        long movieRowId = ContentUris.parseId(movieUri);

        assertTrue(movieRowId != -1);

        // Record has been inserted. Now, check it.
        Cursor c = mContext.getContentResolver().query(
                MovieProvider.Movie.ALL,
                null,
                MovieColumns._ID + "= ?",
                new String[]{movieValues.getAsString(MovieColumns._ID)},
                null
        );

        TestUtilities.validateCursor("", c, movieValues);
    }

    public void testDeleteMovie() {
        testInsertMovie();

        // Register an observer for deletion
        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(MovieProvider.Movie.ALL, true, tco);

        deleteAllRecordsFromProvider();

        // Test to make sure our observer is called.  If not, throw an assertion.
        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);
    }

    public void testBulkInsertMovie() {
        setupProvider();

        ContentValues[] movieValues = TestUtilities.createArrayMovieValues();

        // Register an observer for bulk insert
        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(MovieProvider.Movie.ALL, true, tco);

        int count = mContext.getContentResolver().bulkInsert(
                MovieProvider.Movie.ALL,
                movieValues
        );

        assertTrue(count == 3);

        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        // Movies have been inserted, now check them
        Cursor c = mContext.getContentResolver().query(
                MovieProvider.Movie.ALL,
                null,
                null,
                null,
                MovieColumns._ID + " ASC"
        );

        assertTrue(c.getCount() != 0);

        c.moveToFirst();

        for (ContentValues movieValue : movieValues) {
            TestUtilities.validateCurrentRecord("", c, movieValue);
            c.moveToNext();
        }

        c.close();
    }

    public void testQueryTrailer() {
        setupProvider();
        insertMovie();

        ContentValues trailerValue = TestUtilities.createTrailerValues();

        mContext.getContentResolver().insert(
                MovieProvider.Trailer.ALL,
                trailerValue
        );

        Cursor c = mContext.getContentResolver().query(
                MovieProvider.Trailer.ALL,
                null,
                null,
                null,
                null
        );

        assertTrue(c.getCount() != 0);

        TestUtilities.validateCursor("", c, trailerValue);
    }

    public void testQueryTrailerByMovieId() {
        setupProvider();
        insertMovie();

        ContentValues[] trailerValues = TestUtilities.createArrayTrailerValues();

        mContext.getContentResolver().bulkInsert(
                MovieProvider.Trailer.ALL,
                trailerValues
        );

        Cursor c = mContext.getContentResolver().query(
                MovieProvider.Trailer.ofMovie(333484),
                null,
                null,
                null,
                TrailerColumns._ID + " ASC"
        );

        ArrayList<ContentValues> trailersOfMovie = new ArrayList<>();
        for (ContentValues value : trailerValues) {
            if ((int) value.get(TrailerColumns.MOVIE_ID) == 333484) {
                trailersOfMovie.add(value);
            }
        }

        assertTrue(c.getCount() != 0);

        c.moveToFirst();
        for (ContentValues value : trailersOfMovie) {
            TestUtilities.validateCurrentRecord("", c, value);
            c.moveToNext();
        }
    }

    public void testUpdateTrailer() {
        setupProvider();
        insertMovie();

        ContentValues trailerValue = TestUtilities.createTrailerValues();

        Uri trailerUri = mContext.getContentResolver().insert(
                MovieProvider.Trailer.ALL,
                trailerValue
        );

        long trailerRowId = ContentUris.parseId(trailerUri);
        assertTrue(trailerRowId != -1);

        ContentValues updatedTrailerValue = new ContentValues(trailerValue);
        updatedTrailerValue.put(TrailerColumns.NAME, "Unofficial Trailer");

        Cursor c = mContext.getContentResolver().query(
                MovieProvider.Trailer.ALL,
                null,
                null,
                null,
                null
        );

        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        c.registerContentObserver(tco); // the provider has to notify the content observer about the changes

        int count = mContext.getContentResolver().update(
                MovieProvider.Trailer.ALL,
                updatedTrailerValue,
                TrailerColumns._ID + " = ?",
                new String[]{String.valueOf(trailerValue.getAsString(TrailerColumns._ID))}
        );

        assertTrue(count == 1);

        // Test to make sure our observer is called.  If not, throw an assertion.
        tco.waitForNotificationOrFail();
        c.unregisterContentObserver(tco);
        c.close();

        c = mContext.getContentResolver().query(
                MovieProvider.Trailer.ALL,
                null,
                TrailerColumns._ID + "= ?",
                new String[]{trailerValue.getAsString(TrailerColumns._ID)},
                null
        );

        TestUtilities.validateCursor("", c, updatedTrailerValue);
    }

    public void testInsertTrailer() {
        setupProvider();
        insertMovie();

        ContentValues trailerValues = TestUtilities.createTrailerValues();

        // Register an observer for the insert
        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(MovieProvider.Trailer.ALL, true, tco);

        Uri trailerUri = mContext.getContentResolver().insert(
                MovieProvider.Trailer.ALL,
                trailerValues
        );

        // Test to make sure the observer is called. If not, throw an assertion.
        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        long trailerRowId = ContentUris.parseId(trailerUri);

        assertTrue(trailerRowId != -1);

        // Record has been inserted. Now, check it.
        Cursor c = mContext.getContentResolver().query(
                MovieProvider.Trailer.ALL,
                null,
                TrailerColumns._ID + " = ?",
                new String[]{trailerValues.getAsString(TrailerColumns._ID)},
                null
        );

        TestUtilities.validateCursor("", c, trailerValues);
    }

    public void testDeleteTrailer() {
        testInsertTrailer();

        // Register an observer for deletion
        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(MovieProvider.Trailer.ALL, true, tco);

        deleteAllRecordsFromProvider();

        // Test to make sure our observer is calles. If not, throw an assertion.
        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);
    }

    public void testBulkInsertTrailer() {
        setupProvider();
        insertMovie();

        ContentValues[] trailerValues = TestUtilities.createArrayTrailerValues();

        // Register an observer for bulk insert
        TestUtilities.TestContentObserver tco = TestUtilities.TestContentObserver.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(MovieProvider.Trailer.ALL, true, tco);

        int count = mContext.getContentResolver().bulkInsert(
                MovieProvider.Trailer.ALL,
                trailerValues
        );

        assertTrue(count > 0);

        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        // Trailers have been inserted, now check them
        Cursor c = mContext.getContentResolver().query(
                MovieProvider.Trailer.ALL,
                null,
                null,
                null,
                TrailerColumns._ID + " ASC"
        );

        assertTrue(c.getCount() != 0);

        c.moveToFirst();

        for (ContentValues trailerValue : trailerValues) {
            TestUtilities.validateCurrentRecord("", c, trailerValue);
            c.moveToNext();
        }

        c.close();
    }

    public void testQueryReview() {
        setupProvider();
        insertMovie();

        ContentValues reviewValue = TestUtilities.createReviewValues();

        mContext.getContentResolver().insert(
                MovieProvider.Review.ALL,
                reviewValue
        );

        Cursor c = mContext.getContentResolver().query(
                MovieProvider.Review.ALL,
                null,
                null,
                null,
                null
        );

        assertTrue(c.getCount() != 0);

        TestUtilities.validateCursor("", c, reviewValue);
    }

    public void testQueryReviewByMovieId() {
        setupProvider();
        insertMovie();

        ContentValues[] reviewValues = TestUtilities.createArrayReviewValues();

        mContext.getContentResolver().bulkInsert(
                MovieProvider.Review.ALL,
                reviewValues
        );

        Cursor c = mContext.getContentResolver().query(
                MovieProvider.Review.ofMovie(333484),
                null,
                null,
                null,
                ReviewColumns._ID + " ASC"
        );

        ArrayList<ContentValues> reviewsOfMovie = new ArrayList<>();
        for (ContentValues value : reviewValues) {
            if ((int) value.get(ReviewColumns.MOVIE_ID) == 333484) {
                reviewsOfMovie.add(value);
            }
        }

        assertTrue(c.getCount() != 0);

        c.moveToFirst();
        for (ContentValues value : reviewsOfMovie) {
            TestUtilities.validateCurrentRecord("", c, value);
            c.moveToNext();
        }
    }

    public void testUpdateReview() {
        setupProvider();
        insertMovie();

        ContentValues reviewValue = TestUtilities.createReviewValues();

        Uri reviewUri = mContext.getContentResolver().insert(
                MovieProvider.Review.ALL,
                reviewValue
        );

        long reviewRowId = ContentUris.parseId(reviewUri);
        assertTrue(reviewRowId != -1);

        ContentValues updatedReviewValue = new ContentValues(reviewValue);
        updatedReviewValue.put(ReviewColumns.CONTENT, "Something else");

        Cursor c = mContext.getContentResolver().query(
                MovieProvider.Review.ALL,
                null,
                null,
                null,
                null
        );

        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        c.registerContentObserver(tco); // the provider has to notify the content observer about the changes

        int count = mContext.getContentResolver().update(
                MovieProvider.Review.ALL,
                updatedReviewValue,
                ReviewColumns._ID + " = ?",
                new String[]{String.valueOf(reviewValue.getAsString(ReviewColumns._ID))}
        );

        assertTrue(count == 1);

        // Test to make sure our observer is called.  If not, throw an assertion.
        tco.waitForNotificationOrFail();
        c.unregisterContentObserver(tco);
        c.close();

        c = mContext.getContentResolver().query(
                MovieProvider.Review.ALL,
                null,
                ReviewColumns._ID + " = ?",
                new String[]{reviewValue.getAsString(ReviewColumns._ID)},
                null
        );

        TestUtilities.validateCursor("", c, updatedReviewValue);
    }

    public void testInsertReview() {
        setupProvider();
        insertMovie();

        ContentValues reviewValues = TestUtilities.createReviewValues();

        // Register an observer for the insert
        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(MovieProvider.Review.ALL, true, tco);

        Uri reviewUri = mContext.getContentResolver().insert(
                MovieProvider.Review.ALL,
                reviewValues
        );

        // Test to make sure the observer is called. If not, throw an assertion.
        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        long reviewRowId = ContentUris.parseId(reviewUri);

        assertTrue(reviewRowId != -1);

        // Record has been inserted. Now, check it.
        Cursor c = mContext.getContentResolver().query(
                MovieProvider.Review.ALL,
                null,
                ReviewColumns._ID + " = ?",
                new String[]{reviewValues.getAsString(ReviewColumns._ID)},
                null
        );

        TestUtilities.validateCursor("", c, reviewValues);
    }

    public void testDeleteReview() {
        testInsertTrailer();

        // Register an observer for deletion
        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(MovieProvider.Review.ALL, true, tco);

        deleteAllRecordsFromProvider();

        // Test to make sure our observer is calles. If not, throw an assertion.
        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);
    }

    public void testBulkInsertReview() {
        setupProvider();
        insertMovie();

        ContentValues[] reviewValues = TestUtilities.createArrayReviewValues();

        // Register an observer for bulk insert
        TestUtilities.TestContentObserver tco = TestUtilities.TestContentObserver.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(MovieProvider.Review.ALL, true, tco);

        int count = mContext.getContentResolver().bulkInsert(
                MovieProvider.Review.ALL,
                reviewValues
        );

        assertTrue(count > 0);

        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        // Trailers have been inserted, now check them
        Cursor c = mContext.getContentResolver().query(
                MovieProvider.Review.ALL,
                null,
                null,
                null,
                ReviewColumns._ID + " ASC"
        );

        assertTrue(c.getCount() != 0);

        c.moveToFirst();

        for (ContentValues reviewValue : reviewValues) {
            TestUtilities.validateCurrentRecord("", c, reviewValue);
            c.moveToNext();
        }

        c.close();
    }

    public long insertMovie() {
        ContentValues movieValue = TestUtilities.createMovieValues();
        Uri movieUri = mContext.getContentResolver().insert(
                MovieProvider.Movie.ALL,
                movieValue
        );

        long movieRowId = ContentUris.parseId(movieUri);

        assertTrue(movieRowId != -1);

        return movieRowId;
    }

    private void setupProvider() {
        try {
            setUp();
        } catch (Exception e) {
            assertTrue("Error: Problem when setting the content provider", false);
        }
    }
}
