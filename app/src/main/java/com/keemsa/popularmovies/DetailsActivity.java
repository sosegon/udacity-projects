package com.keemsa.popularmovies;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class DetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        /* TODO: I don't add the fragment programatically
           I simply added the fragment in the xml file as oppose
           to what was done in Sunshine app.
           Is there any disadvantage related to this approach?
         */
    }
}
