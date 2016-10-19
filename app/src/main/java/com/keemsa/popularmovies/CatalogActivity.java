package com.keemsa.popularmovies;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.keemsa.popularmovies.fragment.CatalogFragment;

public class CatalogActivity extends AppCompatActivity {

    private final String LOG_TAG = CatalogActivity.class.getSimpleName();
    private final String CATALOG_FRAGMENT_TAG = "CFTAG";
    private String mQueryBy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.frl_container, new CatalogFragment(), CATALOG_FRAGMENT_TAG)
                    .commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String queryBy = Utility.getPreferredQueryBy(this);

        if (queryBy != null && queryBy.equals(mQueryBy)) {
            CatalogFragment cf = (CatalogFragment) getSupportFragmentManager().findFragmentByTag(CATALOG_FRAGMENT_TAG);
            if (cf != null) {
                cf.onQueryByChanged();
            }
            mQueryBy = queryBy;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.catalog_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.mit_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
