package com.keemsa.popularmovies;


import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

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

    public CatalogFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout
        View view = inflater.inflate(R.layout.fragment_catalog, container, false);

        // Create adapter
        movieAdapter = new MovieAdapter(getContext(), 0, new ArrayList<Movie>());

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
    public void onStart() {
        super.onStart();

        // Verify network connection to fetch movies
        ConnectivityManager manager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            updateMovieCatalog();
        } else {
            Toast.makeText(getContext(), R.string.msg_no_connection, Toast.LENGTH_LONG).show();
        }
    }

    private void updateMovieCatalog() {
        // Construct uri
        String baseUrl = getString(R.string.base_query_url);
        String url = Uri.parse(baseUrl).buildUpon()
                .appendPath("popular")
                .appendQueryParameter("api_key", BuildConfig.MOVIEDB_API_KEY)
                .build()
                .toString();
        MoviesAsyncTask task = new MoviesAsyncTask(this);
        task.execute(url);
    }

    @Override
    public void processJSON(String json) {
        if (json == null || json.length() == 0) {
            return;
        }

        try {
            List<Movie> movies = processMovies(json);
            if (movieAdapter != null) {
                movieAdapter.clear();

                for (Movie movie : movies) {
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
