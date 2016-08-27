package com.keemsa.sunshine;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        String forecastData = intent.getStringExtra(Intent.EXTRA_TEXT);
        ((TextView) findViewById(R.id.txt_forecast)).setText(forecastData);
    }
}
