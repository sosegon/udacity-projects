package com.udacity.gradle.builditbigger;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.keemsa.android.jokes.JokeActivity;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements MainActivity.MainFragment {

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

    @Override
    public void handleJoke(String joke) {
        setProgressBarVisibility(ProgressBar.GONE);
        if(joke.equals(RetrieveJokeAsyncTask.DEFAULT_JOKE)){
            joke = getString(R.string.default_joke);
        }
        Intent intent = new Intent(getContext(), JokeActivity.class);
        intent.putExtra(JokeActivity.JOKE_TAG, joke);
        startActivity(intent);
    }
}
