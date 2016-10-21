package com.keemsa.popularmovies.fragment;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

import com.keemsa.popularmovies.BuildConfig;
import com.keemsa.popularmovies.R;
import com.keemsa.popularmovies.Utility;
import com.keemsa.popularmovies.data.MovieColumns;
import com.keemsa.popularmovies.data.MovieProvider;
import com.keemsa.popularmovies.model.Movie;
import com.keemsa.popularmovies.net.MoviesAsyncTask;

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
            MovieColumns.TITLE,
            MovieColumns.SYNOPSIS,
            MovieColumns.POSTER_URL,
            MovieColumns.QUERY_TYPE,
            MovieColumns.RELEASE_DATE,
            MovieColumns.RATING
    };

    private final int MOVIE_ID = 0,
                        MOVIE_TITLE = 1,
                        MOVIE_SYNOPSIS = 2,
                        MOVIE_POSTER_URL = 3,
                        MOVIE_QUERY_TYPE = 4,
                        MOVIE_RELEASE_DATE = 5,
                        MOVIE_RATING = 6;

    private final int MOVIES_LOADED = 1;

    public interface Callback {
        void onItemSelected(Uri movieUri);

        void onEnableDetailsFragment(Uri movieUri);

        boolean hasSinglePane();
    }

    public CatalogFragment() {
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                getContext(),
                MovieProvider.Movie.ALL,
                MOVIE_COLUMNS,
                Utility.queryFilterByQueryBy(getContext()),
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, final Cursor data) {
        movieAdapter.swapCursor(data);

        if (!((Callback) getActivity()).hasSinglePane()) {
            /*
               In two panes, DetailsFragment has to be added once the movies
               are loaded, therefore a Uri can be passed to it, which in turns
               passes it to its children fragment, so they can perform the
               corresponding queries and display the information
               Solution according to http://stackoverflow.com/a/12421522/1065981
             */
            Handler handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what == MOVIES_LOADED) {
                        if (data.moveToFirst()) {
                            Uri movieUri = MovieProvider.Movie.withId(data.getLong(MOVIE_ID));
                            ((CatalogFragment.Callback) getActivity()).onEnableDetailsFragment(movieUri);
                        }

                    }
                }
            };

            handler.sendEmptyMessage(MOVIES_LOADED);
        }
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
                    ((Callback) getActivity()).onItemSelected(MovieProvider.Movie.withId(c.getLong(MOVIE_ID)));
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

            long _id = currentMovie.optLong("id");
            if(movieExists(_id)){
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

    private boolean movieExists (long movieId) {
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

        if(c.moveToFirst()){
            int queryType = c.getInt(MOVIE_QUERY_TYPE);
            boolean[] currentType = Utility.getValuesFromQueryType(queryType);
            String rated = getResources().getStringArray(R.array.prf_values_sort)[1];
            String popular = getResources().getStringArray(R.array.prf_values_sort)[0];
            int newQueryType;
            if(queryBy.equals(rated)){
                newQueryType = Utility.createQueryType(true, currentType[1], currentType[2]);
            }
            else if(queryBy.equals(popular)){
                newQueryType = Utility.createQueryType(currentType[0], true, currentType[2]);
            }
            else {
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
