package com.keemsa.popularmovies;


import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.keemsa.popularmovies.data.MovieColumns;
import com.keemsa.popularmovies.data.MovieProvider;
import com.keemsa.popularmovies.model.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Vector;

/**
 * A simple {@link Fragment} subclass.
 */
public class CatalogFragment extends Fragment implements MoviesAsyncTask.MoviesAsyncTaskReceiver, LoaderManager.LoaderCallbacks<Cursor> {

    private final String LOG_TAG = CatalogFragment.class.getSimpleName();
    private MovieAdapter movieAdapter;
    private ArrayList<Movie> movieList;
    private ProgressBar prg_load;
    private TextView txt_catalog_message;

    private static final int CATALOG_LOADER_ID = 1;

    private final String[] MOVIE_COLUMNS = {
            MovieColumns._ID,
            MovieColumns.POSTER_URL
    };

    private final int MOVIE_ID = 0;
    private final int MOVIE_POSTER_URL = 1;

    public CatalogFragment() {
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                getContext(),
                MovieProvider.Movie.ALL,
                MOVIE_COLUMNS,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        movieAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        movieAdapter.swapCursor(null);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(CATALOG_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout
        View view = inflater.inflate(R.layout.fragment_catalog, container, false);

        prg_load = (ProgressBar) view.findViewById(R.id.prg_load);
        setProgressBarVisibility(View.GONE);

        txt_catalog_message = (TextView) view.findViewById(R.id.txt_catalog_msg);
        setCatalogMessageVisibility(View.GONE);

        // Create adapter
        movieAdapter = new MovieAdapter(getContext(), null, 0);

        // Attach adapter to view
        GridView gridView = (GridView) view.findViewById(R.id.gv_movies);
        gridView.setAdapter(movieAdapter);

        // Set listener to start activity with detailed info about movie
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Cursor c = (Cursor) adapterView.getItemAtPosition(i);
                if (c != null) {
                    Intent intent = new Intent(getContext(), MovieDetailsActivity.class);

                    intent.setData(MovieProvider.Movie.withId(c.getLong(MOVIE_ID)));
                    startActivity(intent);
                }
            }
        });

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCatalogMessageVisibility(View.GONE);
        if (savedInstanceState == null || !savedInstanceState.containsKey("movieList")) {
            fetchMovieCatalog();
        } else {
            movieList = savedInstanceState.getParcelableArrayList("movieList");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("movieList", movieList);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    public void onQueryByChanged() {
        updateMovieCatalog();
        getLoaderManager().restartLoader(CATALOG_LOADER_ID, null, this);
    }

    private void fetchMovieCatalog() {
        // Verify network connection to fetch movies
        ConnectivityManager manager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            updateMovieCatalog();
        } else {
            setCatalogMessageText(getString(R.string.msg_no_connection));
            setCatalogMessageVisibility(View.VISIBLE);
        }
    }

    private void updateMovieCatalog() {
        // Construct uri
        String baseUrl = getString(R.string.base_query_url);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
        String queryBy = Utility.getPreferredQueryBy(getContext());
        String url = Uri.parse(baseUrl).buildUpon()
                .appendPath(queryBy)
                .appendQueryParameter("api_key", BuildConfig.MOVIEDB_API_KEY)
                .build()
                .toString();
        MoviesAsyncTask task = new MoviesAsyncTask(this);
        task.execute(url);
    }

    @Override
    public void setProgressBarVisibility(int value) {
        if (prg_load != null) {
            prg_load.setVisibility(value);
        }
    }

    @Override
    public void setCatalogMessageVisibility(int value) {
        if (txt_catalog_message != null) {
            txt_catalog_message.setVisibility(value);
        }
    }

    @Override
    public void setCatalogMessageText(int value) {
        switch (value) {
            case 0:
                setCatalogMessageText(getString(R.string.msg_no_connection));
                break;
            case 1:
                setCatalogMessageText(getString(R.string.msg_server_error));
        }
    }

    public void setCatalogMessageText(String value) {
        if (txt_catalog_message != null) {
            txt_catalog_message.setText(value);
        }
    }

    @Override
    public void processJSON(String json) {
        if (json == null || json.length() == 0) {
            return;
        }

        try {
            getContext().getContentResolver().delete(MovieProvider.Movie.ALL, null, null);
            Vector<ContentValues> cvMovies = processMovies(json);
            if (cvMovies.size() > 0) {
                ContentValues[] cvArray = new ContentValues[cvMovies.size()];
                cvMovies.toArray(cvArray);
                getContext().getContentResolver().bulkInsert(MovieProvider.Movie.ALL, cvArray);
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error parsing json");
        }
    }

    private Vector<ContentValues> processMovies(String json) throws JSONException {
        JSONObject dataJson = new JSONObject(json);
        JSONArray moviesJson = dataJson.getJSONArray("results");
        Vector<ContentValues> cvVector = new Vector<>(moviesJson.length());

        for (int i = 0; i < moviesJson.length(); i++) {
            JSONObject currentMovie = moviesJson.getJSONObject(i);
            String title = currentMovie.optString("original_title"),
                    synopsis = currentMovie.optString("overview"),
                    posterUrl = Utility.formatPosterUrl(currentMovie.optString("poster_path")),
                    releaseDate = currentMovie.optString("release_date"),
                    rating = currentMovie.optString("vote_average"),
                    _id = currentMovie.optString("id");

            ContentValues cvMovie = new ContentValues();
            cvMovie.put(MovieColumns.TITLE, title);
            cvMovie.put(MovieColumns.SYNOPSIS, synopsis);
            cvMovie.put(MovieColumns.POSTER_URL, posterUrl);
            cvMovie.put(MovieColumns.RELEASE_DATE, releaseDate);
            cvMovie.put(MovieColumns.RATING, rating);
            cvMovie.put(MovieColumns._ID, _id);

            cvVector.add(cvMovie);
        }

        return cvVector;
    }
}
