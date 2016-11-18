package com.keemsa.popularmovies;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.keemsa.popularmovies.fragment.DetailsFragment;

public class DetailsActivity extends AppCompatActivity {

    private final static String LOG_TAG = DetailsActivity.class.getSimpleName();
    private Toolbar tbr;

    /*
       It is used to pass the argument that enables transition
       animation of the view. Currently, the movie poster
    */
    public static final String DETAIL_TRANSITION_ANIMATION = "DTA";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.frl_details_fragment_container, new DetailsFragment(), "")
                    .commit();
        }

        tbr = (Toolbar) findViewById(R.id.tbr_details);
        setSupportActionBar(tbr);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setElevation(0);

         /*
            Wait until the views are ready to start the transition, so hold the transition until
            that. Elements will be ready when the data is loaded that is under onLoadFinished in
            MovieDetailsFragment
         */
        supportPostponeEnterTransition();
    }
}
