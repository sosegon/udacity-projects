package com.keemsa.popularmovies.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.keemsa.popularmovies.BuildConfig;
import com.keemsa.popularmovies.R;
import com.keemsa.popularmovies.Utility;
import com.keemsa.popularmovies.data.MovieColumns;
import com.keemsa.popularmovies.data.MovieProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

/**
 * Created by sebastian on 10/26/16.
 */
public class MoviesSyncAdapter extends AbstractThreadedSyncAdapter {

    private final String LOG_TAG = MoviesSyncAdapter.class.getSimpleName();
    ContentResolver mContentResolver;

    public static final String[] MOVIE_COLUMNS = {
            MovieColumns._ID,
            MovieColumns.TITLE,
            MovieColumns.SYNOPSIS,
            MovieColumns.POSTER_URL,
            MovieColumns.QUERY_TYPE,
            MovieColumns.RELEASE_DATE,
            MovieColumns.RATING
    };

    public static final int MOVIE_ID = 0,
            MOVIE_TITLE = 1,
            MOVIE_SYNOPSIS = 2,
            MOVIE_POSTER_URL = 3,
            MOVIE_QUERY_TYPE = 4,
            MOVIE_RELEASE_DATE = 5,
            MOVIE_RATING = 6;

    public MoviesSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContentResolver = context.getContentResolver();
    }

    /**
     * Taken from https://github.com/udacity/Sunshine-Version-2/blob/lesson_6_sync_adapter_starter_code/sync/SunshineSyncAdapter.java#L32
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Taken from https://github.com/udacity/Sunshine-Version-2/blob/lesson_6_sync_adapter_starter_code/sync/SunshineSyncAdapter.java#L48
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */
        }
        return newAccount;
    }

    @Override
    public void onPerformSync(Account account, Bundle bundle, String s, ContentProviderClient contentProviderClient, SyncResult syncResult) {
        Log.d(LOG_TAG, "onPerformSync Called");

        String baseUrl = getContext().getString(R.string.base_query_url);
        String queryBy = Utility.getPreferredQueryBy(getContext());
        String url = Uri.parse(baseUrl).buildUpon()
                .appendPath(queryBy)
                .appendQueryParameter("api_key", BuildConfig.MOVIEDB_API_KEY)
                .build()
                .toString();

        processJson(fetchMoviesData(url));
    }

    private String fetchMoviesData(String url) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        String moviesJson = null;

        try {
            URL movieUrl = new URL(url);
            connection = (HttpURLConnection) movieUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            InputStream stream = connection.getInputStream();

            if (stream != null) {
                reader = new BufferedReader(new InputStreamReader(stream));
                String line;
                StringBuilder output = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }

                if (output.length() != 0) {
                    moviesJson = output.toString();
                }
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error connecting to the server to fetch movies' data");
        } finally {
            if (connection != null) {
                connection.disconnect();
            }

            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Error closing stream");
                }
            }
        }

        return moviesJson;
    }

    public void processJson(String json) {
        if (json == null || json.length() == 0) {
            return;
        }

        try {
            Vector<ContentValues> cvMovies = processMovies(json);
            if (cvMovies.size() > 0) {
                ContentValues[] cvArray = new ContentValues[cvMovies.size()];
                cvMovies.toArray(cvArray);
                getContext().getContentResolver().bulkInsert(MovieProvider.Movie.ALL, cvArray);
                //getLoaderManager().restartLoader(CATALOG_CURSOR_LOADER_ID, null, cursorLoader);
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error parsing json data of movies");
        }
    }

    private Vector<ContentValues> processMovies(String json) throws JSONException {
        JSONObject dataJson = new JSONObject(json);
        JSONArray moviesJson = dataJson.getJSONArray("results");
        Vector<ContentValues> cvVector = new Vector<>(moviesJson.length());

        for (int i = 0; i < moviesJson.length(); i++) {
            JSONObject currentMovie = moviesJson.getJSONObject(i);

            long _id = currentMovie.optLong("id");
            if (movieExists(_id)) {
                updateQueryType(_id, Utility.getPreferredQueryBy(getContext()));
                continue;
            }

            String title = currentMovie.optString("original_title"),
                    synopsis = currentMovie.optString("overview"),
                    posterUrl = Utility.formatPosterUrl(currentMovie.optString("poster_path")),
                    releaseDate = currentMovie.optString("release_date"),
                    rating = currentMovie.optString("vote_average");

            ContentValues cvMovie = new ContentValues();
            cvMovie.put(MovieColumns._ID, _id);
            cvMovie.put(MovieColumns.TITLE, title);
            cvMovie.put(MovieColumns.SYNOPSIS, synopsis);
            cvMovie.put(MovieColumns.POSTER_URL, posterUrl);
            cvMovie.put(MovieColumns.RELEASE_DATE, releaseDate);
            cvMovie.put(MovieColumns.RATING, rating);
            // At this point QueryBy is popular or rating
            cvMovie.put(MovieColumns.QUERY_TYPE, Utility.queryTypeByQueryBy(getContext()));

            cvVector.add(cvMovie);
        }

        return cvVector;
    }

    private boolean movieExists(long movieId) {
        return Utility.movieExists(getContext(), movieId);
    }

    private boolean updateQueryType(long movieId, String queryBy) {
        Cursor c = getContext().getContentResolver().query(
                MovieProvider.Movie.withId(movieId),
                MOVIE_COLUMNS,
                null,
                null,
                null
        );

        if (c.moveToFirst()) {
            int queryType = c.getInt(MOVIE_QUERY_TYPE);
            boolean[] currentType = Utility.getValuesFromQueryType(queryType);
            String rated = getContext().getResources().getStringArray(R.array.prf_values_sort)[1];
            String popular = getContext().getResources().getStringArray(R.array.prf_values_sort)[0];
            int newQueryType;
            if (queryBy.equals(rated)) {
                newQueryType = Utility.createQueryType(true, currentType[1], currentType[2]);
            } else if (queryBy.equals(popular)) {
                newQueryType = Utility.createQueryType(currentType[0], true, currentType[2]);
            } else {
                newQueryType = queryType;
            }

            ContentValues cvMovie = new ContentValues();
            cvMovie.put(MovieColumns._ID, c.getLong(MOVIE_ID));
            cvMovie.put(MovieColumns.TITLE, c.getString(MOVIE_TITLE));
            cvMovie.put(MovieColumns.SYNOPSIS, c.getString(MOVIE_SYNOPSIS));
            cvMovie.put(MovieColumns.POSTER_URL, c.getString(MOVIE_POSTER_URL));
            cvMovie.put(MovieColumns.RELEASE_DATE, c.getInt(MOVIE_RELEASE_DATE));
            cvMovie.put(MovieColumns.RATING, c.getFloat(MOVIE_RATING));
            cvMovie.put(MovieColumns.QUERY_TYPE, newQueryType);

            int i = getContext().getContentResolver().update(
                    MovieProvider.Movie.withId(movieId),
                    cvMovie,
                    null,
                    null
            );

            return i >= 0;
        }

        return false;
    }
}
