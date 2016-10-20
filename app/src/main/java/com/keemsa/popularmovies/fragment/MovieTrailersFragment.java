package com.keemsa.popularmovies.fragment;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.keemsa.popularmovies.BuildConfig;
import com.keemsa.popularmovies.R;
import com.keemsa.popularmovies.Utility;
import com.keemsa.popularmovies.data.MovieProvider;
import com.keemsa.popularmovies.data.TrailerColumns;
import com.keemsa.popularmovies.net.TrailersAsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Vector;

/**
 * Created by sebastian on 10/16/16.
 */
public class MovieTrailersFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, TrailersAsyncTask.AsyncTaskReceiver {

    private final String LOG_TAG = MovieTrailersFragment.class.getSimpleName();
    private TrailerAdapter trailerAdapter;
    private ListView lv_trailers;

    private Uri mMovieUri;
    private int mFetchFromServerCount = 0;
    private long mMovieId;

    private static final int TRAILERS_LOADER_ID = 1;
    final static String[] TRAILER_COLUMNS = {
            TrailerColumns._ID,
            TrailerColumns.KEY,
            TrailerColumns.NAME,
            TrailerColumns.SITE
    };
    final static int TRAILER_ID = 0;
    final static int TRAILER_KEY = 1;
    final static int TRAILER_NAME = 2;
    final static int TRAILER_SITE = 3;

    public MovieTrailersFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null) {
            mMovieUri = args.getParcelable(DetailsFragment.MOVIE_URI);
        }

        View view = inflater.inflate(R.layout.fragment_movie_trailers, container, false);

        lv_trailers = (ListView) view.findViewById(R.id.lv_trailers);
        trailerAdapter = new TrailerAdapter(getContext(), null, 0);
        lv_trailers.setAdapter(trailerAdapter);

        lv_trailers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Cursor c = (Cursor) adapterView.getItemAtPosition(i);
                if (c != null) {
                    String site = c.getString(TRAILER_SITE);
                    String key = c.getString(TRAILER_KEY);
                    if (site != null && key != null) {
                        Uri trailerUrl = Utility.createTrailerUri(c.getString(TRAILER_SITE), c.getString(TRAILER_KEY));
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(trailerUrl);
                        startActivity(intent);
                    }
                }

            }
        });

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFetchFromServerCount = 0;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This happens in tablets
        if (mMovieUri != null) {
            long movieId = Long.parseLong(mMovieUri.getLastPathSegment());
            Uri trailerUri = MovieProvider.Trailer.ofMovie(movieId);
            mMovieId = movieId;

            return new CursorLoader(
                    getContext(),
                    trailerUri,
                    TRAILER_COLUMNS,
                    null,
                    null,
                    null
            );
        }

        // This happens in phones
        Intent intent = getActivity().getIntent();
        if (intent == null) {
            return null;
        }

        long movieId = Long.parseLong(intent.getData().getLastPathSegment());
        Uri trailerUri = MovieProvider.Trailer.ofMovie(movieId);
        mMovieId = movieId;
        return new CursorLoader(
                getContext(),
                trailerUri,
                TRAILER_COLUMNS,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.e(LOG_TAG, "Records for trailer of movie: " + data.getCount());
        trailerAdapter.swapCursor(data);
        /*
           If there are no trailers for the movie in the provider,
           proceed to fetch trailers from the server. After results
           are obtained, the loader has to be restarted, which may
           result in no trailers again, because it was not possible
           to fetch them or there are no trailers in the server.
           By checking mFetchFromServerCount, infinite loop is avoided.
         */
        if (data.getCount() == 0 && mFetchFromServerCount == 0) {
            fetchTrailers();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        trailerAdapter.swapCursor(null);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(TRAILERS_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void processJson(String json) {
        if (json == null || json.length() == 0) {
            return;
        }

        try {
            Vector<ContentValues> cvTrailers = processTrailers(json);
            if (cvTrailers.size() > 0) {
                ContentValues[] cvArray = new ContentValues[cvTrailers.size()];
                cvTrailers.toArray(cvArray);
                getContext().getContentResolver().bulkInsert(MovieProvider.Trailer.ALL, cvArray);
                getLoaderManager().restartLoader(TRAILERS_LOADER_ID, null, this);
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error parsing json data of trailers");
        }
    }

    private void fetchTrailers() {
        // Verify network connection to fetch reviews
        ConnectivityManager manager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = manager.getActiveNetworkInfo();

        if (netInfo != null && netInfo.isConnected()) {
            getTrailersFromServer();
        }
    }

    private void getTrailersFromServer() {
        // Construct the url
        String baseUrl = getString(R.string.base_query_url);
        String url = Uri.parse(baseUrl).buildUpon()
                .appendPath("" + mMovieId)
                .appendPath("videos")
                .appendQueryParameter("api_key", BuildConfig.MOVIEDB_API_KEY)
                .build()
                .toString();
        TrailersAsyncTask task = new TrailersAsyncTask(this);
        task.execute(url);
        mFetchFromServerCount++;
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
        return Utility.trailerExists(getContext(), trailerId);
    }
}
