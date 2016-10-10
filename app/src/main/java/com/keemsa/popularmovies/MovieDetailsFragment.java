package com.keemsa.popularmovies;


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

import com.keemsa.popularmovies.data.MovieColumns;
import com.squareup.picasso.Picasso;


/**
 * A simple {@link Fragment} subclass.
 */
public class MovieDetailsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String LOG_TAG = MovieDetailsFragment.class.getSimpleName();

    private TextView txt_title_details,
            txt_year_details,
            txt_score_details,
            txt_desc_details;

    private ImageView imv_movie_poster_details;

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

        txt_title_details.setText(title);
        txt_year_details.setText(date);
        txt_score_details.setText("" + score);
        txt_desc_details.setText(desc);
        String fullPosterUrl = Uri.parse(getContext().getString(R.string.base_img_url)).buildUpon().appendPath(posterUrl).build().toString();
        Picasso.with(getContext()).load(fullPosterUrl).into(imv_movie_poster_details);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIE_DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_movie_details, container, false);

        txt_title_details = (TextView) view.findViewById(R.id.txt_title_details);
        txt_year_details = (TextView) view.findViewById(R.id.txt_year_details);
        txt_score_details = (TextView) view.findViewById(R.id.txt_score_details);
        txt_desc_details = (TextView) view.findViewById(R.id.txt_desc_details);
        imv_movie_poster_details = (ImageView) view.findViewById(R.id.imv_movie_poster_details);

        return view;
    }
}
