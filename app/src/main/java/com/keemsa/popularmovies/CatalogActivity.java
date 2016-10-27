package com.keemsa.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.keemsa.popularmovies.fragment.CatalogFragment;
import com.keemsa.popularmovies.fragment.DetailsFragment;
import com.keemsa.popularmovies.sync.MoviesSyncAdapter;

public class CatalogActivity extends AppCompatActivity implements CatalogFragment.Callback {

    private final String LOG_TAG = CatalogActivity.class.getSimpleName();
    private final String DETAILS_FRAGMENT_TAG = "DFTAG";
    private String mQueryBy;
    private boolean mTwoPane = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        /*
           No need to add the DetailsFragment if the container is present.
           That is because the fragments within DetailsFragment fetch data
           either from the server or the content provider. In any case, they
           need a Uri to get the data. That Uri can only be provided
           when the movies have been loaded in CatalogFragment.
         */

        mTwoPane = findViewById(R.id.frl_details_container) != null;

        getSupportActionBar().setElevation(0);

        MoviesSyncAdapter.initializeSyncAdapter(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String queryBy = Utility.getPreferredQueryBy(this);

        if (queryBy != null && !queryBy.equals(mQueryBy)) {
            CatalogFragment cf = (CatalogFragment) getSupportFragmentManager().findFragmentById(R.id.frg_catalog);
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

    @Override
    public boolean hasSinglePane() {
        return !mTwoPane;
    }

    @Override
    public void onEnableDetailsFragment(Uri movieUri) {
        Bundle args = new Bundle();
        args.putParcelable(DetailsFragment.MOVIE_URI, movieUri);

        /*
           The Bundle is passed to DetailsFragment, but since
           it's just a container for other fragments, it passes
           the bundle to them
         */
        DetailsFragment frg = new DetailsFragment();
        frg.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frl_details_container, frg)
                .commit();
    }

    @Override
    public void onItemSelected(Uri movieUri) {
        if (mTwoPane) {
            Bundle args = new Bundle();
            args.putParcelable(DetailsFragment.MOVIE_URI, movieUri);

            DetailsFragment frg = new DetailsFragment();
            frg.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frl_details_container, frg, DETAILS_FRAGMENT_TAG)
                    .commit();

        } else {
            Intent intent = new Intent(this, DetailsActivity.class)
                    .setData(movieUri);
            startActivity(intent);
        }
    }
}
