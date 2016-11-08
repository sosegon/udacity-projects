package com.keemsa.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTabHost;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.keemsa.popularmovies.fragment.CatalogFragment;
import com.keemsa.popularmovies.fragment.DetailsFragment;
import com.keemsa.popularmovies.fragment.SearchFragment;
import com.keemsa.popularmovies.gcm.RegistrationIntentService;
import com.keemsa.popularmovies.sync.MoviesSyncAdapter;

public class CatalogActivity extends AppCompatActivity implements MovieSelectedInterface {

    private final String LOG_TAG = CatalogActivity.class.getSimpleName();
    private final String DETAILS_FRAGMENT_TAG = "DFTAG";
    private final String CATALOG_FRAGMENT_TAG = "CFTAG";
    private final String SEARCH_FRAGMENT_TAG = "SFTAG";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
    private String mQueryBy;
    private boolean mTwoPane = false;
    private FragmentTabHost mtabHost;
    private Toolbar tbr;


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

        mtabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);

        mtabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);

        mtabHost.addTab(
                mtabHost.newTabSpec(CATALOG_FRAGMENT_TAG).setIndicator(getString(R.string.lbl_catalog), null),
                CatalogFragment.class, null
        );

        mtabHost.addTab(
                mtabHost.newTabSpec(SEARCH_FRAGMENT_TAG).setIndicator(getString(R.string.lbl_search), null),
                SearchFragment.class, null
        );

        tbr = (Toolbar) findViewById(R.id.tbr_catalog);
        setSupportActionBar(tbr);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setElevation(0);

        MoviesSyncAdapter.initializeSyncAdapter(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String queryBy = Utility.getPreferredQueryBy(this);

        if (queryBy != null && !queryBy.equals(mQueryBy)) {
            CatalogFragment cf = (CatalogFragment) getSupportFragmentManager().findFragmentByTag(CATALOG_FRAGMENT_TAG);
            if (cf != null) {
                cf.onQueryByChanged();
            }
            mQueryBy = queryBy;
        }

        if (!checkPlayServices()) {
            // Because this is the initial creation of the app, we'll want to be certain we have
            // a token. If we do not, then we will start the IntentService that will register this
            // application with GCM.
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            boolean sentToken = sharedPreferences.getBoolean(SENT_TOKEN_TO_SERVER, false);
            if (!sentToken) {
                Intent intent = new Intent(this, RegistrationIntentService.class);
                startService(intent);
            }
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

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(LOG_TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }
}
