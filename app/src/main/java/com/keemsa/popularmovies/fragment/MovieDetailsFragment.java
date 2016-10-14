package com.keemsa.popularmovies.fragment;


import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.keemsa.popularmovies.R;
import com.keemsa.popularmovies.Utility;
import com.keemsa.popularmovies.data.MovieColumns;
import com.keemsa.popularmovies.model.Movie;
import com.squareup.picasso.Picasso;


/**
 * A simple {@link Fragment} subclass.
 */
public class MovieDetailsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String LOG_TAG = MovieDetailsFragment.class.getSimpleName();

    /*
       This view is not part of the fragment layout anymore.
       It has been moved to the activity layout. That was done
       in order to get the title of the movie as a header of
       the fragment tab, so it's displayed for every other fragment
       because any other fragment will display information of the
       same movie
     */
    private TextView txt_title_details;
    private TextView txt_year_details,
            txt_score_details,
            txt_desc_details;

    private ImageView imv_movie_poster_details;

    private Movie mMovie;
    private final int MOVIE_DETAIL_LOADER = 0;
    private final String[] MOVIE_COLUMNS = {
            MovieColumns.TITLE,
            MovieColumns.SYNOPSIS,
            MovieColumns.POSTER_URL,
            MovieColumns.RELEASE_DATE,
            MovieColumns.RATING
    };
    private final int MOVIE_TITLE = 0;
    private final int MOVIE_SYNOPSIS = 1;
    private final int MOVIE_POSTER_URL = 2;
    private final int MOVIE_RELEASE_DATE = 3;
    private final int MOVIE_RATING = 4;

    public MovieDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Intent intent = getActivity().getIntent();

        if (intent == null) {
            return null;
        }

        return new CursorLoader(
                getActivity(),
                intent.getData(),
                MOVIE_COLUMNS,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }

        String title = data.getString(MOVIE_TITLE);
        String date = data.getString(MOVIE_RELEASE_DATE);
        float score = data.getFloat(MOVIE_RATING);
        String desc = data.getString(MOVIE_SYNOPSIS);
        String posterUrl = Utility.formatPosterUrl(data.getString(MOVIE_POSTER_URL));
        String fullPosterUrl = Uri.parse(getContext().getString(R.string.base_img_url)).buildUpon().appendPath(posterUrl).build().toString();

        /*
           Create the mMovie objects to be used
           as a container of data.
         */
        mMovie = new Movie(title, desc, fullPosterUrl, date, score);

        fillViewsWithValues(mMovie);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIE_DETAIL_LOADER, null, this);

        /*
           At this point, it's safe to get the reference to the view
           in the activity
         */
        txt_title_details = (TextView) getActivity().findViewById(R.id.txt_title_details);

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_movie_details, container, false);

        txt_year_details = (TextView) view.findViewById(R.id.txt_year_details);
        txt_score_details = (TextView) view.findViewById(R.id.txt_score_details);
        txt_desc_details = (TextView) view.findViewById(R.id.txt_desc_details);
        imv_movie_poster_details = (ImageView) view.findViewById(R.id.imv_movie_poster_details);

        return view;
    }

    private void fillViewsWithValues(Movie movie) {
        /*
           Since the next View is in the Activity, it's necessary
           to check its existence.
         */
        if (txt_title_details != null) {
            txt_title_details.setText(movie.getTitle());
        }

        txt_year_details.setText(movie.getReleaseDate());
        txt_score_details.setText("" + movie.getRating());
        txt_desc_details.setText(movie.getSynopsis());

        Picasso.with(getContext()).load(movie.getPosterUrl()).into(imv_movie_poster_details);
    }
}
