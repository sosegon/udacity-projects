package com.keemsa.popularmovies.fragment;


import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.keemsa.popularmovies.MovieSelectedInterface;
import com.keemsa.popularmovies.R;
import com.keemsa.popularmovies.Utility;
import com.keemsa.popularmovies.adapter.MovieAdapter;
import com.keemsa.popularmovies.data.MovieProvider;
import com.keemsa.popularmovies.data.Queries;
import com.keemsa.popularmovies.model.Movie;
import com.keemsa.popularmovies.sync.MoviesSyncAdapter;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class CatalogFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private final String LOG_TAG = CatalogFragment.class.getSimpleName();
    private MovieAdapter movieAdapter;
    private ArrayList<Movie> movieList;
    private ProgressBar prg_load;
    private TextView txt_catalog_msg;
    private RecyclerView rv_movies;
    private int mPosition = RecyclerView.NO_POSITION;

    private static final int CATALOG_CURSOR_LOADER_ID = 1;

    private final int MOVIES_LOADED = 1;

    private LoaderManager.LoaderCallbacks cursorLoader = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new CursorLoader(
                    getContext(),
                    MovieProvider.Movie.ALL,
                    Queries.MOVIE_PROJECTION,
                    Utility.queryFilterByQueryBy(getContext()),
                    null,
                    null
            );
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, final Cursor data) {
            Log.i(LOG_TAG, "Records for movies: " + data.getCount());
            movieAdapter.swapCursor(data);

            if (!((MovieSelectedInterface) getActivity()).hasSinglePane()) {
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
                                    Uri movieUri = MovieProvider.Movie.withId(data.getLong(Queries.MOVIE_ID));
                                    ((MovieSelectedInterface) getActivity()).onEnableDetailsFragment(movieUri);
                                }
                            }
                        }
                    };

                    handler.sendEmptyMessage(MOVIES_LOADED);
                }
            }

            if (data.getCount() == 0) {
                MoviesSyncAdapter.syncImmediately(getContext());
            }

            prg_load.setVisibility(View.GONE);

            /*
                This is a workaround to solve the problem of the recycler view
                not updating the elements accordingly when scrolling.
                TODO: Can this cause performance issues?
             */
            rv_movies.setItemViewCacheSize(data.getCount());

            updateEmptyView();
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
        Utility.goLoader(CatalogFragment.this, CATALOG_CURSOR_LOADER_ID, cursorLoader);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout
        View view = inflater.inflate(R.layout.fragment_catalog, container, false);

        prg_load = (ProgressBar) view.findViewById(R.id.prg_load);

        txt_catalog_msg = (TextView) view.findViewById(R.id.txt_catalog_msg);
        txt_catalog_msg.setText(getString(R.string.msg_data_no_available, getString(R.string.lbl_movies).toLowerCase()));

        // Create adapter
        movieAdapter = new MovieAdapter(getContext(), new MovieAdapter.MovieAdapterOnClickHandler() {
            @Override
            public void onClick(long movieId, MovieAdapter.ViewHolder vh) {
                ((MovieSelectedInterface) getActivity()).onItemSelected(MovieProvider.Movie.withId(movieId));
                mPosition = vh.getAdapterPosition();
            }
        }, txt_catalog_msg);

        rv_movies = (RecyclerView) view.findViewById(R.id.rv_movies);

        rv_movies.setHasFixedSize(true);

        // Set a layout manager
        rv_movies.setLayoutManager(new GridLayoutManager(getContext(), 2));

        // Attach adapter to view
        rv_movies.setAdapter(movieAdapter);

        prg_load.setVisibility(View.VISIBLE);

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null || !savedInstanceState.containsKey("movieList")) {
            Utility.goLoader(CatalogFragment.this, CATALOG_CURSOR_LOADER_ID, cursorLoader);
        } else {
            movieList = savedInstanceState.getParcelableArrayList("movieList");
        }
    }

    @Override
    public void onResume() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
        pref.registerOnSharedPreferenceChangeListener(this);
        super.onResume();
    }

    @Override
    public void onPause() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
        pref.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("movieList", movieList);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals(getString(R.string.pref_movies_status_key))) {
            updateEmptyView();
        }
    }

    public void onQueryByChanged() {
        Utility.goLoader(CatalogFragment.this, CATALOG_CURSOR_LOADER_ID, cursorLoader);
    }

    private void updateEmptyView() {
        if (movieAdapter.getItemCount() == 0) {
            if (txt_catalog_msg != null) {
                Utility.updateMoviesEmptyView(getContext(), txt_catalog_msg);
            }
        }
    }
}
