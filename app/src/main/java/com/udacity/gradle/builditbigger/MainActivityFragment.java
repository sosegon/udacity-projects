package com.udacity.gradle.builditbigger;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements MainActivity.ProgressBarBin {

    private static String LOG_TAG = MainActivityFragment.class.getSimpleName();
    private ProgressBar pgr;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_main, container, false);

        pgr = (ProgressBar) root.findViewById(R.id.pgr);

        return root;
    }

    @Override
    public void setProgressBarVisibility(int value) {
        if (pgr != null) {
            pgr.setVisibility(value);
        }
    }
}
