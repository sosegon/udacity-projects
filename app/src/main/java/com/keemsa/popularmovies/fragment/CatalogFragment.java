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
public class CatalogFragment extends Fragment {

    private final String LOG_TAG = CatalogFragment.class.getSimpleName();
    private MovieAdapter movieAdapter;
    private ArrayList<Movie> movieList;
    private ProgressBar prg_load;
    private TextView txt_catalog_message;
    private int mFetchFromServerCount = 0;

    private static final int CATALOG_CURSOR_LOADER_ID = 1;
    private static final int CATALOG_ASYNC_LOADER_ID = 2;

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

    private final int MOVIES_LOADED = 1;

    public interface Callback {
        void onItemSelected(Uri movieUri);

        void onEnableDetailsFragment(Uri movieUri);

        boolean hasSinglePane();
    }

    private LoaderManager.LoaderCallbacks asyncLoader = new LoaderManager.LoaderCallbacks<String>() {
        @Override
        public Loader onCreateLoader(int id, Bundle args) {
            /*
                The app is here because it didn't find trailers with
                the cursor loader. After using this loader, the
                cursor loader will be restarted.
             */
            mFetchFromServerCount++;

            // Verify network connection to fetch trailers
            ConnectivityManager manager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = manager.getActiveNetworkInfo();

            if (netInfo == null || !netInfo.isConnected()) {
                return null;
            }

            return new MoviesAsyncTask(getContext());
        }

        @Override
        public void onLoadFinished(Loader<String> loader, String data) {
            processJson(data);
        }

        @Override
        public void onLoaderReset(Loader<String> loader) {

        }
    };

    private LoaderManager.LoaderCallbacks cursorLoader = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            setProgressBarVisibility(View.VISIBLE);
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
            Log.i(LOG_TAG, "Records for movies: " + data.getCount());
            movieAdapter.swapCursor(data);

            if (!((Callback) getActivity()).hasSinglePane()) {
                /*
                    In two panes, DetailsFragment has to be added once the movies
                    are loaded, therefore a Uri can be passed to it, which in turns
                    passes it to its children fragment, so they can perform the
                    corresponding queries and display the information
                    Solution according to http://stackoverflow.com/a/12421522/1065981
                 */
                if (data.getCount() > 0) {
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
            /*
                If there are no movies with the cursor loader,
                proceed to fetch them using the async loader. After results
                are obtained, the cursor loader has to be restarted, which may
                result in no movies again, because it was not possible to fetch
                them with the async loader or there are no movies in the server.
                By checking mFetchFromServerCount, infinite loop is avoided.
            */
            if (data.getCount() == 0 && mFetchFromServerCount == 0) {
                getLoaderManager().initLoader(CATALOG_ASYNC_LOADER_ID, null, asyncLoader);
            }

            String queryBy = Utility.getPreferredQueryBy(getContext());
            String fav = getResources().getStringArray(R.array.prf_values_sort)[2];
            if (mFetchFromServerCount > 0 || data.getCount() > 0 || queryBy.equals(fav)) {
                setProgressBarVisibility(View.GONE);
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            movieAdapter.swapCursor(null);
        }
    };

    public CatalogFragment() {
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(CATALOG_CURSOR_LOADER_ID, null, cursorLoader);
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

        // Create adapter
        movieAdapter = new MovieAdapter(getContext(), null, 0);

        GridView gridView = (GridView) view.findViewById(R.id.gv_movies);

        // Add empty view
        gridView.setEmptyView(txt_catalog_message);

        // Attach adapter to view
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
        mFetchFromServerCount = 0;
        if (savedInstanceState == null || !savedInstanceState.containsKey("movieList")) {
            getLoaderManager().initLoader(CATALOG_CURSOR_LOADER_ID, null, cursorLoader);
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
        String queryBy = Utility.getPreferredQueryBy(getContext());
        String fav = getResources().getStringArray(R.array.prf_values_sort)[2];
        /*
            If the queryBy variable is popular of rated, then the app has to connect
            to the server to fetch data, and add new movies. Otherwise, it simply queries
            the local content provider.
         */
        mFetchFromServerCount = queryBy.equals(fav) ? 1 : 0;

        getLoaderManager().restartLoader(CATALOG_CURSOR_LOADER_ID, null, cursorLoader);
    }

    public void setProgressBarVisibility(int value) {
        if (prg_load != null) {
            prg_load.setVisibility(value);
        }
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
                getLoaderManager().restartLoader(CATALOG_CURSOR_LOADER_ID, null, cursorLoader);
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
            String rated = getResources().getStringArray(R.array.prf_values_sort)[1];
            String popular = getResources().getStringArray(R.array.prf_values_sort)[0];
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
