package com.keemsa.popularmovies;

import android.net.Uri;

import com.keemsa.popularmovies.adapter.MovieAdapter;

/**
 * Created by sebastian on 10/31/16.
 */
public interface MovieSelectedInterface {
    /*  The ViewHolder needs to be passed because in order to create a transition with shared elements,
        the view with the element to share, and the name of the transition has to be passed as a pair.
        See this in the implementation of the function in CatalogActivity.
     */
    void onItemSelected(Uri movieUri, MovieAdapter.ViewHolder vh);

    void onEnableDetailsFragment(Uri movieUri);

    boolean hasSinglePane();
}
