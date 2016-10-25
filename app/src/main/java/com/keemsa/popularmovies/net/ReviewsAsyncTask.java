package com.keemsa.popularmovies.net;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.keemsa.popularmovies.BuildConfig;
import com.keemsa.popularmovies.R;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by sebastian on 10/12/16.
 */
public class ReviewsAsyncTask extends AsyncTaskLoader<String> {

    private final String LOG_TAG = TrailersAsyncTask.class.getSimpleName();
    private int response = -1;
    private long mMovieId;
    private String jsonReviews;

    public ReviewsAsyncTask(Context context, long mMovieId) {
        super(context);
        this.mMovieId = mMovieId;
    }

    @Override
    public String loadInBackground() {
        String baseUrl = getContext().getString(R.string.base_query_url);
        String url = Uri.parse(baseUrl).buildUpon()
                .appendPath("" + mMovieId)
                .appendPath("reviews")
                .appendQueryParameter("api_key", BuildConfig.MOVIEDB_API_KEY)
                .build()
                .toString();

        return fetchMovieReviews(url);
    }

    @Override
    public void deliverResult(String data) {
        super.deliverResult(data);
        jsonReviews = data;
    }

    // As stated in http://stackoverflow.com/a/7481941/1065981
    @Override
    protected void onStartLoading() {
        if (jsonReviews != null) {
            deliverResult(jsonReviews);
        }

        if (takeContentChanged() || jsonReviews == null) {
            forceLoad();
        }
    }

    private String fetchMovieReviews(String url) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        String reviewsJson = null;

        try {
            URL reviewUrl = new URL(url);
            connection = (HttpURLConnection) reviewUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            // Used in onPostExecute
            response = connection.getResponseCode();

            InputStream stream = connection.getInputStream();

            if (stream != null) {
                reader = new BufferedReader(new InputStreamReader(stream));
                String line;
                StringBuilder output = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }

                if (output.length() != 0) {
                    reviewsJson = output.toString();
                }
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error");
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

        return reviewsJson;
    }
}
