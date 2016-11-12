package com.keemsa.popularmovies.fragment;


import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
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
import com.keemsa.popularmovies.data.Queries;
import com.keemsa.popularmovies.model.Movie;
import com.keemsa.popularmovies.service.DbService;
import com.keemsa.popularmovies.ui.Typewriter;
import com.squareup.picasso.Picasso;

import java.io.File;


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
    private Typewriter txt_title_details;
    private TextView txt_year_details,
            txt_score_details,
            txt_desc_details;

    private ImageView imv_movie_poster_details,
                        imv_movie_fav_details;

    private Uri mMovieUri;
    private Movie mMovie;
    private final int MOVIE_DETAIL_LOADER = 0;

    public MovieDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (mMovieUri != null) {
            return new CursorLoader(
                    getActivity(),
                    mMovieUri,
                    Queries.MOVIE_PROJECTION,
                    null,
                    null,
                    null
            );
        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null || !data.moveToFirst()) {
            return;
        }

        String title = data.getString(Queries.MOVIE_TITLE);
        String date = data.getString(Queries.MOVIE_RELEASE_DATE);
        float score = data.getFloat(Queries.MOVIE_RATING);
        String desc = data.getString(Queries.MOVIE_SYNOPSIS);
        String posterUrl = Utility.formatPosterUrl(data.getString(Queries.MOVIE_POSTER_URL));
        String fullPosterUrl = Uri.parse(getContext().getString(R.string.base_img_url)).buildUpon().appendPath(posterUrl).build().toString();
        int queryType = data.getInt(Queries.MOVIE_QUERY_TYPE);
        long id = data.getLong(Queries.MOVIE_ID);

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
        txt_title_details = (Typewriter) getActivity().findViewById(R.id.txt_title_details);

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // This happens in tablets
        Bundle args = getArguments();
        if (args != null) {
            mMovieUri = args.getParcelable(DetailsFragment.MOVIE_URI);
        }

        // This happens in phones
        Intent intent = getActivity().getIntent();
        if (intent != null) {
            mMovieUri = intent.getData();
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
            // Based on http://stackoverflow.com/a/6700718/1065981
            /*
                TODO: The animation works but it would be better to have an animation
                from right to left
             */
            txt_title_details.setCharacterDelay(50);
            txt_title_details.animateText(movie.getTitle());
        }

        txt_year_details.setText(movie.getReleaseDate());
        txt_score_details.setText("" + movie.getRating());
        txt_desc_details.setText(movie.getSynopsis());

        String posterUrl = Uri.parse(movie.getPosterUrl()).getLastPathSegment();
        ContextWrapper cw = new ContextWrapper(getContext());
        File directory = cw.getDir(Utility.getPosterDirectory(getContext()), Context.MODE_PRIVATE);
        File posterFile = new File(directory, posterUrl);
        if (posterFile.exists()) { // At this point the image is already stored locally, but just in case.
            Picasso.with(getContext()).load(posterFile).fit().into(imv_movie_poster_details);
        }

        boolean fav = Utility.isFavourite(movie.getQueryType());
        imv_movie_fav_details.setImageResource(fav ? R.drawable.ic_fav : R.drawable.ic_nonfav);
    }

    private void toggleMovieFav(){
        boolean newFav = !Utility.isFavourite(mMovie.getQueryType());
        imv_movie_fav_details.setImageResource(newFav ? R.drawable.ic_fav : R.drawable.ic_nonfav);

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

        // update the record in db
        Intent intent = new Intent(getContext(), DbService.class);
        intent.setData(MovieProvider.Movie.withId(mMovie.getId()));
        intent.putExtra(DbService.DB_SERVICE_OPERATION, DbService.DB_SERVICE_UPDATE);
        intent.putExtra(DbService.DB_SERVICE_VALUE, cv);

        getContext().startService(intent);

        // update member object
        mMovie.setQueryType(newQueryType);
    }
}
