package com.keemsa.popularmovies.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.keemsa.popularmovies.R;

/**
 * Created by sebastian on 10/18/16.
 */
public class DetailsFragment extends Fragment {
    private static final String LOG_TAG = DetailsFragment.class.getSimpleName();
    public static final String MOVIE_URI = "URI";

    private FragmentTabHost mtabHost;

    public DetailsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_details, container, false);

        Bundle args = getArguments();

        mtabHost = (FragmentTabHost) view.findViewById(android.R.id.tabhost);
        /*
           Use getChildFragmentManager instead of getSupportFragmentManager to avoid
           error because of recursion in the two pane mode, when the fragment is added
           right after the movies are loaded.
         */
        mtabHost.setup(getContext(), getChildFragmentManager(), android.R.id.tabcontent);

        mtabHost.addTab(
                mtabHost.newTabSpec("details").setIndicator(getString(R.string.lbl_details), null),
                MovieDetailsFragment.class, args
        );

        mtabHost.addTab(
                mtabHost.newTabSpec("reviews").setIndicator(getString(R.string.lbl_reviews), null),
                MovieReviewsFragment.class, args
        );

        mtabHost.addTab(
                mtabHost.newTabSpec("trailers").setIndicator(getString(R.string.lbl_trailers), null),
                MovieTrailersFragment.class, args
        );

        return view;
    }
}
