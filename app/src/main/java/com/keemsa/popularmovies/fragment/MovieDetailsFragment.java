package com.keemsa.popularmovies.fragment;


import android.content.ContentValues;
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
import com.keemsa.popularmovies.data.MovieProvider;
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

    private ImageView imv_movie_poster_details,
                        imv_movie_fav_details;

    private Uri mMovieUri;
    private Movie mMovie;
    private final int MOVIE_DETAIL_LOADER = 0;
    private final String[] MOVIE_COLUMNS = {
            MovieColumns.TITLE,
            MovieColumns.SYNOPSIS,
            MovieColumns.POSTER_URL,
            MovieColumns.RELEASE_DATE,
            MovieColumns.RATING,
            MovieColumns.QUERY_TYPE,
            MovieColumns._ID
    };
    private final int MOVIE_TITLE = 0;
    private final int MOVIE_SYNOPSIS = 1;
    private final int MOVIE_POSTER_URL = 2;
    private final int MOVIE_RELEASE_DATE = 3;
    private final int MOVIE_RATING = 4;
    private final int MOVIE_QUERY_TYPE = 5;
    private final int MOVIE_ID = 6;

    public MovieDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This happens in tablets
        if (mMovieUri != null) {
            return new CursorLoader(
                    getActivity(),
                    mMovieUri,
                    MOVIE_COLUMNS,
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
        if (data == null || !data.moveToFirst()) {
            return;
        }

        String title = data.getString(MOVIE_TITLE);
        String date = data.getString(MOVIE_RELEASE_DATE);
        float score = data.getFloat(MOVIE_RATING);
        String desc = data.getString(MOVIE_SYNOPSIS);
        String posterUrl = Utility.formatPosterUrl(data.getString(MOVIE_POSTER_URL));
        String fullPosterUrl = Uri.parse(getContext().getString(R.string.base_img_url)).buildUpon().appendPath(posterUrl).build().toString();
        int queryType = data.getInt(MOVIE_QUERY_TYPE);
        long id = data.getLong(MOVIE_ID);

        /*
           Create the mMovie objects to be used
           as a container of data.
         */
        mMovie = new Movie(title, desc, fullPosterUrl, date, score);
        mMovie.setQueryType(queryType);
        mMovie.setId(id);

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
        Bundle args = getArguments();
        if (args != null) {
            mMovieUri = args.getParcelable(DetailsFragment.MOVIE_URI);
        }

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_movie_details, container, false);

        txt_year_details = (TextView) view.findViewById(R.id.txt_year_details);
        txt_score_details = (TextView) view.findViewById(R.id.txt_score_details);
        txt_desc_details = (TextView) view.findViewById(R.id.txt_desc_details);
        imv_movie_poster_details = (ImageView) view.findViewById(R.id.imv_movie_poster_details);
        imv_movie_fav_details = (ImageView) view.findViewById(R.id.imv_movie_fav_details);

        imv_movie_fav_details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleMovieFav();
            }
        });

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
        boolean fav = Utility.isFavourite(movie.getQueryType());
        imv_movie_fav_details.setImageResource(fav ? R.drawable.ic_fav : R.drawable.ic_nonfav);
    }

    private void toggleMovieFav(){
        boolean newFav = !Utility.isFavourite(mMovie.getQueryType());
        imv_movie_fav_details.setImageResource(newFav ? R.drawable.ic_fav : R.drawable.ic_nonfav);

        // TODO: Do this when leaving the fragment to improve performance by avoiding multiple db operations
        // update the record in db
        ContentValues cv = new ContentValues();
        cv.put(MovieColumns._ID, mMovie.getId());
        cv.put(MovieColumns.TITLE, mMovie.getTitle());
        cv.put(MovieColumns.SYNOPSIS, mMovie.getSynopsis());
        cv.put(MovieColumns.RELEASE_DATE, mMovie.getReleaseDate());
        // Be careful here since posterUrl of the object is not the same value of posterUrl of record in db
        cv.put(MovieColumns.POSTER_URL, Uri.parse(mMovie.getPosterUrl()).getLastPathSegment());
        cv.put(MovieColumns.RATING, mMovie.getRating());

        boolean prevTypes[] = Utility.getValuesFromQueryType(mMovie.getQueryType());
        int newQueryType = Utility.createQueryType(prevTypes[0], prevTypes[1], newFav);
        cv.put(MovieColumns.QUERY_TYPE, newQueryType);

        getContext().getContentResolver().update(
                MovieProvider.Movie.withId(mMovie.getId()),
                cv,
                null,
                null
        );

        // update member object
        mMovie.setQueryType(newQueryType);
    }
}
