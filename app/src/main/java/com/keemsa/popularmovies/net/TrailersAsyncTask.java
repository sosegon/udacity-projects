package com.keemsa.popularmovies.net;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by sebastian on 10/16/16.
 */
public class TrailersAsyncTask extends AsyncTask<String, Void, String> {

    public interface AsyncTaskReceiver {
        void processJson(String json);
    }

    private final String LOG_TAG = TrailersAsyncTask.class.getSimpleName();
    private AsyncTaskReceiver receiver;
    private int response = -1;

    public TrailersAsyncTask(AsyncTaskReceiver receiver) {
        this.receiver = receiver;
    }

    @Override
    protected String doInBackground(String... urls) {
        if (urls != null && urls.length > 0) {
            return fetchMovieTrailers(urls[0]);
        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if (s != null) {
            receiver.processJson(s);
        }
    }

    private String fetchMovieTrailers(String url) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        String trailersJson = null;

        try {
            URL trailerUrl = new URL(url);
            connection = (HttpURLConnection) trailerUrl.openConnection();
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
                    trailersJson = output.toString();
                }
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error connecting to the server to fetch movie trailers data");
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

        return trailersJson;
    }
}
