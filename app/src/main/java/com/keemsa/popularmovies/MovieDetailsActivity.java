package com.keemsa.popularmovies;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;

import com.keemsa.popularmovies.fragment.MovieDetailsFragment;
import com.keemsa.popularmovies.fragment.MovieReviewsFragment;
import com.keemsa.popularmovies.fragment.MovieTrailersFragment;

public class MovieDetailsActivity extends FragmentActivity {

    private FragmentTabHost mtabHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_movie_details);
        mtabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mtabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);

        mtabHost.addTab(
                mtabHost.newTabSpec("details").setIndicator(getString(R.string.lbl_details), null),
                MovieDetailsFragment.class, null
        );

        mtabHost.addTab(
                mtabHost.newTabSpec("reviews").setIndicator(getString(R.string.lbl_reviews), null),
                MovieReviewsFragment.class, null
        );

        mtabHost.addTab(
                mtabHost.newTabSpec("trailers").setIndicator(getString(R.string.lbl_trailers), null),
                MovieTrailersFragment.class, null
        );

    }
}
