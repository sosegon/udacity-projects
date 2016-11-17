package com.keemsa.popularmovies;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.keemsa.popularmovies.fragment.DetailsFragment;

public class DetailsActivity extends AppCompatActivity {

    private Toolbar tbr;

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
    }
}
