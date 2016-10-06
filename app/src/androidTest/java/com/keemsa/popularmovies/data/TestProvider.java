package com.keemsa.popularmovies.data;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import android.test.AndroidTestCase;

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

        assertTrue("Error: Provider has some previous records", c.getCount() == 0);
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

        // content://com.keemsa.popularmovies/movie/#/trailer
        type = mContext.getContentResolver().getType(MovieProvider.Trailer.ofMovie(123));
        assertEquals("Error: Type for " + MovieProvider.Trailer.ofMovie(123) + " is wrong", MovieProvider.Trailer.CONTENT_DIR_TYPE, type);

        // content://com.keemsa.popularmovies/trailer/*
        type = mContext.getContentResolver().getType(MovieProvider.Trailer.withId("wexc"));
        assertEquals("Error: Type for " + MovieProvider.Trailer.withId("wexc") + " is wrong", MovieProvider.Trailer.CONTENT_ITEM_TYPE, type);

        // content://com.keemsa.popularmovies/movie/#/review
        type = mContext.getContentResolver().getType(MovieProvider.Review.ofMovie(123));
        assertEquals("Error: Type for " + MovieProvider.Review.ofMovie(123) + " is wrong", MovieProvider.Review.CONTENT_DIR_TYPE, type);

        // content://com.keemsa.popularmovies/review/*
        type = mContext.getContentResolver().getType(MovieProvider.Review.withId("wexc"));
        assertEquals("Error: Type for " + MovieProvider.Review.withId("wexc") + " is wrong", MovieProvider.Review.CONTENT_ITEM_TYPE, type);
    }

    public void testBasicMovieQuery() {
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

    public void testMultipleMovieQuery() {
        setupProvider();

        ContentValues[] movieValues = TestUtilities.createArrayMovieValues();

        mContext.getContentResolver().bulkInsert(
                MovieProvider.Movie.ALL,
                movieValues
        );

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

    public void testBulkInsert() {
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

        // Movies has been inserted, now check them
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

    private void setupProvider() {
        try {
            setUp();
        } catch (Exception e) {
            assertTrue("Error: Problem when setting the content provider", false);
        }
    }
}
