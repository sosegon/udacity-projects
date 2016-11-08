package com.keemsa.popularmovies;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class DetailsActivity extends AppCompatActivity {

    private Toolbar tbr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        /* TODO: I don't add the fragment programatically
           I simply added the fragment in the xml file as oppose
           to what was done in Sunshine app.
           Is there any disadvantage related to this approach?
         */

        tbr = (Toolbar) findViewById(R.id.tbr_details);
        setSupportActionBar(tbr);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setElevation(0);
    }
}
