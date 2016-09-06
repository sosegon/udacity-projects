package com.keemsa.popularmovies;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.keemsa.popularmovies.model.Movie;
import com.squareup.picasso.Picasso;


/**
 * A simple {@link Fragment} subclass.
 */
public class MovieDetailsFragment extends Fragment {

    private final String LOG_TAG = MovieDetailsFragment.class.getSimpleName();

    private TextView txt_title_details,
            txt_year_details,
            txt_score_details,
            txt_desc_details;

    private ImageView imv_movie_poster_details;

    public MovieDetailsFragment() {
        // Required empty public constructor
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

        Intent intent = getActivity().getIntent();

        if (intent != null && intent.hasExtra("movie")) {
            Movie movie = intent.getParcelableExtra("movie");
            txt_title_details.setText(movie.getTitle());
            txt_year_details.setText(String.valueOf(movie.year()));
            txt_score_details.setText(String.valueOf(movie.getRating()) + "/10");
            txt_desc_details.setText(movie.getSynopsis());

            String posterFullUrl = getContext().getString(R.string.base_img_url) + movie.getPosterUrl();
            Picasso.with(getContext()).load(posterFullUrl).into(imv_movie_poster_details);
        }

        return view;
    }
}
