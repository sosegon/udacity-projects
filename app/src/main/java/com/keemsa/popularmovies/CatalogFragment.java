package com.keemsa.popularmovies;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.keemsa.popularmovies.model.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class CatalogFragment extends Fragment implements MoviesAsyncTask.MoviesAsyncTaskReceiver {

    private final String LOG_TAG = CatalogFragment.class.getSimpleName();
    private MovieAdapter movieAdapter;
    private ArrayList<Movie> movieList;
    private ProgressBar prg_load;
    private TextView txt_catalog_message;

    public CatalogFragment() {
        setHasOptionsMenu(true);
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

        movieList = new ArrayList<Movie>();

        // Create adapter
        movieAdapter = new MovieAdapter(getContext(), 0, movieList);

        // Attach adapter to view
        GridView gridView = (GridView) view.findViewById(R.id.gv_movies);
        gridView.setAdapter(movieAdapter);

        // Set listener to start activity with detailed info about movie
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getContext(), MovieDetailsActivity.class);
                intent.putExtra("movie", movieAdapter.getItem(i));
                startActivity(intent);
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
        fetchMovieCatalog();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // inflate menu
        inflater.inflate(R.menu.catalog_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.mit_settings:
                Intent intent = new Intent(getContext(), SettingsActivity.class);
                startActivity(intent);
                break;

        }

        return super.onOptionsItemSelected(item);
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
        String sortBy = pref.getString(getString(R.string.prf_key_sort), getString(R.string.prf_default_sort));
        String url = Uri.parse(baseUrl).buildUpon()
                .appendPath(sortBy)
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
            movieList = (ArrayList) processMovies(json);
            if (movieAdapter != null) {
                movieAdapter.clear();

                for (Movie movie : movieList) {
                    movieAdapter.add(movie);
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error parsing json");
        }
    }

    private List<Movie> processMovies(String json) throws JSONException {
        JSONObject dataJson = new JSONObject(json);
        JSONArray moviesJson = dataJson.getJSONArray("results");
        List<Movie> movies = new ArrayList<Movie>();

        for (int i = 0; i < moviesJson.length(); i++) {
            JSONObject currentMovie = moviesJson.getJSONObject(i);
            String title = currentMovie.optString("original_title"),
                    synopsis = currentMovie.optString("overview"),
                    posterUrl = currentMovie.optString("poster_path"),
                    releaseDate = currentMovie.optString("release_date"),
                    rating = currentMovie.optString("vote_average");

            Movie movie = new Movie(title, synopsis, posterUrl, releaseDate, rating);
            movies.add(movie);
        }

        return movies;
    }
}
