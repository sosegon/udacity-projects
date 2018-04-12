package com.keemsa.tourguide;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewPager vpg_places = (ViewPager) findViewById(R.id.vpg_places);

        PlaceFragmentPagerAdapter adapter = new PlaceFragmentPagerAdapter(this, getSupportFragmentManager());

        vpg_places.setAdapter(adapter);

        TabLayout tby_places = (TabLayout) findViewById(R.id.tby_places);

        tby_places.setupWithViewPager(vpg_places);
    }
}
