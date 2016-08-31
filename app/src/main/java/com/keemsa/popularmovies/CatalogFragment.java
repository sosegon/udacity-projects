package com.keemsa.popularmovies;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.keemsa.popularmovies.model.Movie;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class CatalogFragment extends Fragment {

    private final String LOG_TAG = CatalogFragment.class.getSimpleName();

    public CatalogFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout
        View view = inflater.inflate(R.layout.fragment_catalog, container, false);

        // Create adapter
        MovieAdapter adapter = new MovieAdapter(getContext(), 0, new ArrayList<Movie>());

        // Attach adapter to view
        ListView listView = (ListView) view.findViewById(R.id.lv_movies);
        listView.setAdapter(adapter);

        return view;
    }
}
