package com.keemsa.popularmovies;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by sebastian on 31/08/16.
 */
public class MoviesAsyncTask extends AsyncTask<String, Void, String> {

    public interface MoviesAsyncTaskReceiver {
        void processJSON(String json);
    }

    private final String LOG_TAG = MoviesAsyncTask.class.getSimpleName();
    private int response = -1;
    private MoviesAsyncTaskReceiver receiver;

    public MoviesAsyncTask(MoviesAsyncTaskReceiver receiver) {
        this.receiver = receiver;
    }

    @Override
    protected String doInBackground(String... strings) {
        return fetchMoviesData(strings[0]);
    }

    @Override
    protected void onPostExecute(String s) {
        if (response != 200) {
            // TODO add code to handle invalid response from server
            return;
        }

        receiver.processJSON(s);
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
                    moviesJson = output.toString();
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

        return moviesJson;
    }
}
