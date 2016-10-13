package com.keemsa.popularmovies.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;

import com.keemsa.popularmovies.Utility;
import com.keemsa.popularmovies.utils.PollingCheck;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

/**
 * Created by sebastian on 10/4/16.
 */
public class TestUtilities extends AndroidTestCase {

    static ContentValues createMovieValues() {
        ContentValues movieValues = new ContentValues();
        movieValues.put(MovieColumns._ID, 333484);
        movieValues.put(MovieColumns.TITLE, "The Magnificent Seven");
        movieValues.put(MovieColumns.SYNOPSIS, "A big screen remake of John Sturges' classic western The Magnificent Seven, itself a remake of Akira Kurosawa's Seven Samurai. Seven gun men in the old west gradually come together to help a poor village against savage thieves.");
        movieValues.put(MovieColumns.POSTER_URL, "z6BP8yLwck8mN9dtdYKkZ4XGa3D.jpg");
        movieValues.put(MovieColumns.RELEASE_DATE, Utility.getDateInMilliSeconds("2016-09-14"));
        movieValues.put(MovieColumns.RATING, 4.7);

        return movieValues;
    }

    static ContentValues[] createArrayMovieValues() {
        ContentValues movieValues1 = new ContentValues();
        movieValues1.put(MovieColumns._ID, 333484);
        movieValues1.put(MovieColumns.TITLE, "The Magnificent Seven");
        movieValues1.put(MovieColumns.SYNOPSIS, "A big screen remake of John Sturges' classic western The Magnificent Seven, itself a remake of Akira Kurosawa's Seven Samurai. Seven gun men in the old west gradually come together to help a poor village against savage thieves.");
        movieValues1.put(MovieColumns.POSTER_URL, "z6BP8yLwck8mN9dtdYKkZ4XGa3D.jpg");
        movieValues1.put(MovieColumns.RELEASE_DATE, Utility.getDateInMilliSeconds("2016-09-14"));
        movieValues1.put(MovieColumns.RATING, 4.7);

        ContentValues movieValues2 = new ContentValues();
        movieValues2.put(MovieColumns._ID, 271110);
        movieValues2.put(MovieColumns.TITLE, "Captain America: Civil War");
        movieValues2.put(MovieColumns.SYNOPSIS, "Following the events of Age of Ultron, the collective governments of the world pass an act designed to regulate all superhuman activity. This polarizes opinion amongst the Avengers, causing two factions to side with Iron Man or Captain America, which causes an epic battle between former allies.");
        movieValues2.put(MovieColumns.POSTER_URL, "5N20rQURev5CNDcMjHVUZhpoCNC.jpg");
        movieValues2.put(MovieColumns.RELEASE_DATE, Utility.getDateInMilliSeconds("2016-04-27"));
        movieValues2.put(MovieColumns.RATING, 6.77);

        ContentValues movieValues3 = new ContentValues();
        movieValues3.put(MovieColumns._ID, 278924);
        movieValues3.put(MovieColumns.TITLE, "Mechanic: Resurrection");
        movieValues3.put(MovieColumns.SYNOPSIS, "Arthur Bishop thought he had put his murderous past behind him when his most formidable foe kidnaps the love of his life. Now he is forced to travel the globe to complete three impossible assassinations, and do what he does best, make them look like accidents.");
        movieValues3.put(MovieColumns.POSTER_URL, "tgfRDJs5PFW20Aoh1orEzuxW8cN.jpg");
        movieValues3.put(MovieColumns.RELEASE_DATE, Utility.getDateInMilliSeconds("2016-08-25"));
        movieValues3.put(MovieColumns.RATING, 4.34);

        return new ContentValues[]{movieValues2, movieValues3, movieValues1}; // ASC orderBy ID
    }

    static ContentValues createTrailerValues() {
        ContentValues trailerValues = new ContentValues();
        trailerValues.put(TrailerColumns._ID, "5797609fc3a36865ae0021cd");
        trailerValues.put(TrailerColumns.KEY, "q-RBA0xoaWU");
        trailerValues.put(TrailerColumns.SITE, "YouTube");
        trailerValues.put(TrailerColumns.NAME, "Official Trailer");
        trailerValues.put(TrailerColumns.TYPE, "Trailer");
        trailerValues.put(TrailerColumns.MOVIE_ID, 333484);

        return trailerValues;
    }

    static ContentValues[] createArrayTrailerValues() {
        ContentValues trailerValues1 = new ContentValues();
        trailerValues1.put(TrailerColumns._ID, "5797609fc3a36865ae0021cd");
        trailerValues1.put(TrailerColumns.KEY, "q-RBA0xoaWU");
        trailerValues1.put(TrailerColumns.SITE, "YouTube");
        trailerValues1.put(TrailerColumns.NAME, "Official Trailer");
        trailerValues1.put(TrailerColumns.TYPE, "Trailer");
        trailerValues1.put(TrailerColumns.MOVIE_ID, 333484);

        ContentValues trailerValues2 = new ContentValues();
        trailerValues2.put(TrailerColumns._ID, "57183f21c3a3687b8c002e3b");
        trailerValues2.put(TrailerColumns.KEY, "deSRpSn8Pyk");
        trailerValues2.put(TrailerColumns.SITE, "YouTube");
        trailerValues2.put(TrailerColumns.NAME, "Teaser Trailer");
        trailerValues2.put(TrailerColumns.TYPE, "Teaser");
        trailerValues2.put(TrailerColumns.MOVIE_ID, 333484);

        ContentValues trailerValues3 = new ContentValues();
        trailerValues3.put(TrailerColumns._ID, "5794ccaa9251414236001173");
        trailerValues3.put(TrailerColumns.KEY, "43NWzay3W4s");
        trailerValues3.put(TrailerColumns.SITE, "YouTube");
        trailerValues3.put(TrailerColumns.NAME, "Official Trailer #1");
        trailerValues3.put(TrailerColumns.TYPE, "Trailer");
        trailerValues3.put(TrailerColumns.MOVIE_ID, 271110);

        ContentValues trailerValues4 = new ContentValues();
        trailerValues4.put(TrailerColumns._ID, "5738f0ac92514166fe000fb6");
        trailerValues4.put(TrailerColumns.KEY, "dKrVegVI0Us");
        trailerValues4.put(TrailerColumns.SITE, "YouTube");
        trailerValues4.put(TrailerColumns.NAME, "Official Trailer 2");
        trailerValues4.put(TrailerColumns.TYPE, "Trailer");
        trailerValues4.put(TrailerColumns.MOVIE_ID, 271110);

        ContentValues trailerValues5 = new ContentValues();
        trailerValues5.put(TrailerColumns._ID, "5794f51392514179d2004179");
        trailerValues5.put(TrailerColumns.KEY, "QF903RaKLvs");
        trailerValues5.put(TrailerColumns.SITE, "YouTube");
        trailerValues5.put(TrailerColumns.NAME, "Official Trailer");
        trailerValues5.put(TrailerColumns.TYPE, "Trailer");
        trailerValues5.put(TrailerColumns.MOVIE_ID, 278924);

        ContentValues values[] = {trailerValues1, trailerValues2, trailerValues3, trailerValues4, trailerValues5};
        Arrays.sort(values, new Comparator<ContentValues>() {
            @Override
            public int compare(ContentValues contentValues, ContentValues t1) {
                return contentValues.getAsString(TrailerColumns._ID).compareTo(t1.getAsString(TrailerColumns._ID));
            }
        });

        return values; // ASC orderBy ID
    }

    static ContentValues createReviewValues() {
        ContentValues reviewValues = new ContentValues();
        reviewValues.put(ReviewColumns._ID, "57e8d5e9c3a3687c180059c9");
        reviewValues.put(ReviewColumns.AUTHOR, "Frank Ochieng");
        reviewValues.put(ReviewColumns.CONTENT, "Some review here");
        reviewValues.put(ReviewColumns.URL, "https://www.themoviedb.org/review/57e8d5e9c3a3687c180059c9");
        reviewValues.put(ReviewColumns.MOVIE_ID, 333484);

        return reviewValues;
    }

    static ContentValues[] createArrayReviewValues() {
        ContentValues reviewValues1 = new ContentValues();
        reviewValues1.put(ReviewColumns._ID, "57e8d5e9c3a3687c180059c9");
        reviewValues1.put(ReviewColumns.AUTHOR, "Frank Ochieng");
        reviewValues1.put(ReviewColumns.CONTENT, "Some review 1 here");
        reviewValues1.put(ReviewColumns.URL, "https://www.themoviedb.org/review/57e8d5e9c3a3687c180059c9");
        reviewValues1.put(ReviewColumns.MOVIE_ID, 333484);

        ContentValues reviewValues2 = new ContentValues();
        reviewValues2.put(ReviewColumns._ID, "57eb62e9c3a36836f10021c8");
        reviewValues2.put(ReviewColumns.AUTHOR, "Sebastian Brownlow");
        reviewValues2.put(ReviewColumns.CONTENT, "Some review 2 here");
        reviewValues2.put(ReviewColumns.URL, "https://www.themoviedb.org/review/57eb62e9c3a36836f10021c8");
        reviewValues2.put(ReviewColumns.MOVIE_ID, 333484);

        ContentValues reviewValues3 = new ContentValues();
        reviewValues3.put(ReviewColumns._ID, "572d7bc1c3a3680fdb001d69");
        reviewValues3.put(ReviewColumns.AUTHOR, "Frank Ochieng");
        reviewValues3.put(ReviewColumns.CONTENT, "Some review 3 here");
        reviewValues3.put(ReviewColumns.URL, "https://www.themoviedb.org/review/572d7bc1c3a3680fdb001d69");
        reviewValues3.put(ReviewColumns.MOVIE_ID, 271110);

        ContentValues reviewValues4 = new ContentValues();
        reviewValues4.put(ReviewColumns._ID, "57acf05cc3a36820750001c8");
        reviewValues4.put(ReviewColumns.AUTHOR, "Austin Singleton");
        reviewValues4.put(ReviewColumns.CONTENT, "Some review 4 here");
        reviewValues4.put(ReviewColumns.URL, "https://www.themoviedb.org/review/57acf05cc3a36820750001c8");
        reviewValues4.put(ReviewColumns.MOVIE_ID, 271110);

        ContentValues reviewValues5 = new ContentValues();
        reviewValues5.put(ReviewColumns._ID, "57f40f319251417fe300171a");
        reviewValues5.put(ReviewColumns.AUTHOR, "Reno");
        reviewValues5.put(ReviewColumns.CONTENT, "Some review 5 here");
        reviewValues5.put(ReviewColumns.URL, "https://www.themoviedb.org/review/57f40f319251417fe300171a");
        reviewValues5.put(ReviewColumns.MOVIE_ID, 271110);

        ContentValues reviewValues6 = new ContentValues();
        reviewValues6.put(ReviewColumns._ID, "57c3994b925141532a002173");
        reviewValues6.put(ReviewColumns.AUTHOR, "Frank Ochieng");
        reviewValues6.put(ReviewColumns.CONTENT, "Some review 6 here");
        reviewValues6.put(ReviewColumns.URL, "https://www.themoviedb.org/review/57c3994b925141532a002173");
        reviewValues6.put(ReviewColumns.MOVIE_ID, 278924);

        ContentValues values[] = {reviewValues1, reviewValues2, reviewValues3, reviewValues4, reviewValues5, reviewValues6};
        Arrays.sort(values, new Comparator<ContentValues>() {
            @Override
            public int compare(ContentValues contentValues, ContentValues t1) {
                return contentValues.getAsString(ReviewColumns._ID).compareTo(t1.getAsString(ReviewColumns._ID));
            }
        });

        return values; // ASC orderBy ID
    }

    // The next method comes from
    // https://github.com/udacity/Sunshine-Version-2/blob/sunshine_master/app/src/androidTest/java/com/example/android/sunshine/app/data/TestUtilities.java#L27
    static void validateCursor(String error, Cursor cursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, cursor.moveToFirst());
        validateCurrentRecord(error, cursor, expectedValues);
        cursor.close();
    }

    // The next method comes from
    // https://github.com/udacity/Sunshine-Version-2/blob/sunshine_master/app/src/androidTest/java/com/example/android/sunshine/app/data/TestUtilities.java#L33
    static void validateCurrentRecord(String error, Cursor cursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valuesInRecord = expectedValues.valueSet();

        for (Map.Entry<String, Object> value : valuesInRecord) {
            String columnName = value.getKey();
            int columnIndex = cursor.getColumnIndex(columnName);
            assertFalse("Column " + columnName + " not found" + error, columnIndex == -1);

            String expectedValue = value.getValue().toString();
            String actualValue = cursor.getString(columnIndex);
            assertEquals(
                    "Value " + actualValue + " did not match expected value " + expectedValue + ". " + error,
                    expectedValue, actualValue);
        }
    }

    static void validateUriType(Context mContext, String error, Uri uri, String expectedUriType) {
        String type = mContext.getContentResolver().getType(uri);
        assertEquals(
                "Type " + type + " did not match expected type " + expectedUriType + ". " + error,
                expectedUriType,
                type
        );
    }

    // The next class comes from
    // https://github.com/udacity/Sunshine-Version-2/blob/sunshine_master/app/src/androidTest/java/com/example/android/sunshine/app/data/TestUtilities.java#L107
    static class TestContentObserver extends ContentObserver {
        final HandlerThread mHT;
        boolean mContentChanged;

        static TestContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        private TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }

        // On earlier versions of Android, this onChange method is called
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

        public void waitForNotificationOrFail() {
            // Note: The PollingCheck class is taken from the Android CTS (Compatibility Test Suite).
            // It's useful to look at the Android CTS source for ideas on how to test your Android
            // applications.  The reason that PollingCheck works is that, by default, the JUnit
            // testing framework is not running on the main Android application thread.
            new PollingCheck(5000) {
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();
            mHT.quit();
        }
    }

    // This method comes from
    // https://github.com/udacity/Sunshine-Version-2/blob/sunshine_master/app/src/androidTest/java/com/example/android/sunshine/app/data/TestUtilities.java#L148
    static TestContentObserver getTestContentObserver() {
        return TestContentObserver.getTestContentObserver();
    }
}