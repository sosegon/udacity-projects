package com.keemsa.popularmovies;

import android.net.Uri;

/**
 * Created by sebastian on 10/31/16.
 */
public interface MovieSelectedInterface {
    void onItemSelected(Uri movieUri);

    void onEnableDetailsFragment(Uri movieUri);

    boolean hasSinglePane();
}
