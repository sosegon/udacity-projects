package com.keemsa.popularmovies.fragment;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.keemsa.popularmovies.AppStatus;
import com.keemsa.popularmovies.MovieSelectedInterface;
import com.keemsa.popularmovies.R;
import com.keemsa.popularmovies.Utility;
import com.keemsa.popularmovies.adapter.MovieAdapter;
import com.keemsa.popularmovies.data.MovieColumns;
import com.keemsa.popularmovies.data.MovieProvider;
import com.keemsa.popularmovies.net.MoviesAsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Vector;

/**
 * Created by sebastian on 10/28/16.
 */
public class SearchFragment extends Fragment {

    private final String LOG_TAG = SearchFragment.class.getSimpleName();
    private MovieAdapter movieAdapter;
    private ProgressBar prg_load;
    private ImageView imv_search;
    private EditText etx_search;
    private TextView txt_search_msg;
    private RecyclerView rv_search;
    private int mPosition = RecyclerView.NO_POSITION;

    private String mKeyword;

    private final int MOVIE_CURSOR_LOADER_ID = 0;
    private final int MOVIE_ASYNC_LOADER_ID = 1;

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

    private LoaderManager.LoaderCallbacks asyncLoader = new LoaderManager.LoaderCallbacks<String>() {
        @Override
        public Loader onCreateLoader(int id, Bundle args) {
            return new MoviesAsyncTask(getContext());
        }

        @Override
        public void onLoadFinished(Loader<String> loader, String data) {
            processJson(data);
        }

        @Override
        public void onLoaderReset(Loader loader) {
            movieAdapter.swapCursor(null);
        }
    };

    private LoaderManager.LoaderCallbacks cursorLoader = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader onCreateLoader(int id, Bundle args) {
            return new CursorLoader(
                    getContext(),
                    MovieProvider.Movie.ALL,
                    MOVIE_COLUMNS,
                    MovieColumns.QUERY_TYPE + " = ? and " + MovieColumns.TITLE + " like '%" + mKeyword + "%'",
                    new String[]{"0"}, // 0 is the value when movies are gotten from search
                    null
            );
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            movieAdapter.swapCursor(data);
            prg_load.setVisibility(View.GONE);

            /*
                Avoid wrong update of the elements. The same situation that
                happens in the catalog fragment
             */
            rv_search.setItemViewCacheSize(data.getCount());

            updateEmptyView();
        }

        @Override
        public void onLoaderReset(Loader loader) {
            movieAdapter.swapCursor(null);
        }
    };

    public SearchFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        prg_load = (ProgressBar) view.findViewById(R.id.prg_load_search);
        imv_search = (ImageView) view.findViewById(R.id.imv_search);
        etx_search = (EditText) view.findViewById(R.id.etx_search);
        txt_search_msg = (TextView) view.findViewById(R.id.txt_search_msg);
        rv_search = (RecyclerView) view.findViewById(R.id.rv_movies_search);

        // Create adapter
        movieAdapter = new MovieAdapter(getContext(), new MovieAdapter.MovieAdapterOnClickHandler() {
            @Override
            public void onClick(long movieId, MovieAdapter.ViewHolder vh) {
                ((MovieSelectedInterface) getActivity()).onItemSelected(MovieProvider.Movie.withId(movieId));
                mPosition = vh.getAdapterPosition();
            }
        }, txt_search_msg);

        rv_search = (RecyclerView) view.findViewById(R.id.rv_movies_search);

        rv_search.setHasFixedSize(true);

        // Set a layout manager
        rv_search.setLayoutManager(new GridLayoutManager(getContext(), 2));

        // Attach adapter to view
        rv_search.setAdapter(movieAdapter);

        imv_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mKeyword = etx_search.getText().toString().toLowerCase();
                Utility.setPreferredValue(
                        getContext(),
                        getContext().getString(R.string.pref_search_keyword_key),
                        mKeyword,
                        true
                );

                /*
                    Clear the adapter to avoid records of previous search
                    to be mixed with the records of the new search
                 */
                movieAdapter.swapCursor(null);

                /*
                    Remove all views to avoid data from previous search.
                    I guess this happens because I'm using setItemViewCacheSize
                    to avoid the wrong update of elements when scrolling.
                 */
                rv_search.removeAllViews();

                Utility.goLoader(SearchFragment.this, MOVIE_ASYNC_LOADER_ID, asyncLoader);
                prg_load.setVisibility(View.VISIBLE);
            }
        });

        return view;
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
            }
            Utility.goLoader(SearchFragment.this, MOVIE_CURSOR_LOADER_ID, cursorLoader);
            Utility.setMoviesStatus(getContext(), AppStatus.MOVIES_STATUS_OK);

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error parsing json data of movies");
            Utility.setMoviesStatus(getContext(), AppStatus.MOVIES_STATUS_SERVER_INVALID);
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
                // No need to update the record
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
            // At this point query_type is 0 = search
            cvMovie.put(MovieColumns.QUERY_TYPE, 0);

            cvVector.add(cvMovie);
        }

        return cvVector;
    }

    private boolean movieExists(long movieId) {
        return Utility.movieExists(getContext(), movieId);
    }

    private void updateEmptyView() {
        if (movieAdapter.getItemCount() == 0) {
            if (txt_search_msg != null) {
                Utility.updateMoviesEmptyView(getContext(), txt_search_msg);
            }
        }
    }
}
