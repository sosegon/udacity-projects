package com.keemsa.popularmovies.net;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.keemsa.popularmovies.AppStatus;
import com.keemsa.popularmovies.BuildConfig;
import com.keemsa.popularmovies.R;
import com.keemsa.popularmovies.Utility;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by sebastian on 31/08/16.
 */
public class MoviesAsyncTask extends AsyncTaskLoader<String> {

    private final String LOG_TAG = TrailersAsyncTask.class.getSimpleName();
    private String jsonMovies;

    public MoviesAsyncTask(Context context) {
        super(context);
    }

    @Override
    public String loadInBackground() {
        String baseUrl = getContext().getString(R.string.base_search_url);
        String searchKeyword = Utility.getPreferredValue(
                getContext(),
                getContext().getString(R.string.pref_search_keyword_key),
                ""
        );
        String url = Uri.parse(baseUrl).buildUpon()
                .appendQueryParameter("query", searchKeyword)
                .appendQueryParameter("api_key", BuildConfig.MOVIEDB_API_KEY)
                .build()
                .toString();

        return fetchMoviesData(url);
    }

    @Override
    public void deliverResult(String data) {
        super.deliverResult(data);
        jsonMovies = data;
    }

    // As stated in http://stackoverflow.com/a/7481941/1065981
    @Override
    protected void onStartLoading() {
        if (jsonMovies != null) {
            deliverResult(jsonMovies);
        }

        if (takeContentChanged() || jsonMovies == null) {
            forceLoad();
        }
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
            } else {
                Utility.setMoviesStatus(getContext(), AppStatus.MOVIES_STATUS_SERVER_DOWN);
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error connecting to the server to fetch movies' data");
            Utility.setMoviesStatus(getContext(), AppStatus.MOVIES_STATUS_SERVER_DOWN);

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
}
