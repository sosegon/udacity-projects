package com.keemsa.popularmovies.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.keemsa.popularmovies.BuildConfig;
import com.keemsa.popularmovies.R;
import com.keemsa.popularmovies.Utility;
import com.keemsa.popularmovies.data.MovieProvider;
import com.keemsa.popularmovies.data.TrailerColumns;

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
 * Created by sebastian on 10/27/16.
 */
public class TrailersService extends IntentService{

    private final static String LOG_TAG = TrailersService.class.getSimpleName();
    public static final String MOVIE_ID = "mid";
    public static final String INVOKER_MESSENGER = "ims";
    public static final String WORK_DONE = "wdn";
    private long mMovieId;

    /*
        TODO: Add flags for network status similar to what is done in MoviesSyncAdapter
     */

    public TrailersService() {
        super("TrailersService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mMovieId = intent.getLongExtra(MOVIE_ID, 0);
        if(mMovieId == 0){
            return;
        }

        String baseUrl = getString(R.string.base_query_url);
        String url = Uri.parse(baseUrl).buildUpon()
                .appendPath("" + mMovieId)
                .appendPath("videos")
                .appendQueryParameter("api_key", BuildConfig.MOVIEDB_API_KEY)
                .build()
                .toString();

        processJson(fetchMovieTrailers(url));
        contactInvoker(intent);
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

    public void processJson(String json) {
        if (json == null || json.length() == 0) {
            return;
        }

        try {
            Vector<ContentValues> cvTrailers = processTrailers(json);
            if (cvTrailers.size() > 0) {
                ContentValues[] cvArray = new ContentValues[cvTrailers.size()];
                cvTrailers.toArray(cvArray);
                getContentResolver().bulkInsert(MovieProvider.Trailer.ALL, cvArray);
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error parsing json data of trailers");
        }
    }

    private Vector<ContentValues> processTrailers(String json) throws JSONException {
        JSONObject dataJson = new JSONObject(json);
        long movieId = dataJson.getLong("id");
        if (movieId != mMovieId) {
            return new Vector<>();
        }

        JSONArray trailersJson = dataJson.getJSONArray("results");
        Vector<ContentValues> cvVector = new Vector<>(trailersJson.length());

        for (int i = 0; i < trailersJson.length(); i++) {
            JSONObject currentTrailer = trailersJson.getJSONObject(i);
            String _id = currentTrailer.optString("id");
            if (trailerExists(_id)) {
                continue;
            }

            String name = currentTrailer.optString("name"),
                    type = currentTrailer.optString("type"),
                    key = currentTrailer.optString("key"),
                    site = currentTrailer.optString("site");

            ContentValues cvTrailer = new ContentValues();
            cvTrailer.put(TrailerColumns._ID, _id);
            cvTrailer.put(TrailerColumns.NAME, name);
            cvTrailer.put(TrailerColumns.KEY, key);
            cvTrailer.put(TrailerColumns.SITE, site);
            cvTrailer.put(TrailerColumns.TYPE, type);
            cvTrailer.put(TrailerColumns.MOVIE_ID, mMovieId);

            cvVector.add(cvTrailer);
        }

        return cvVector;
    }

    private boolean trailerExists(String trailerId) {
        return Utility.trailerExists(this, trailerId);
    }

    // As stated in http://stackoverflow.com/a/7871538/1065981
    private void contactInvoker(Intent intent) {
        Messenger messenger = (Messenger) intent.getExtras().get(INVOKER_MESSENGER);
        Message msg = Message.obtain();
        Bundle data = new Bundle();
        data.putString(WORK_DONE, WORK_DONE);
        msg.setData(data);
        try {
            messenger.send(msg);
        } catch (RemoteException e){
            Log.e(LOG_TAG, e.getMessage());
        }
    }
}
