package com.keemsa.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Scene;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.Spinner;

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
    private Toolbar tbr;
    private Spinner spr_query_mode;
    private FrameLayout frl_catalog_container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        if (savedInstanceState == null) {
            /*
                No need to create the fragment here since this will be done
                below in the spinner section. This will avoid double creation
                and will be more user friendly since it will create the fragment
                base on previous usage (Catalog or Search)
             */
//            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.frl_catalog_container, new CatalogFragment(), CATALOG_FRAGMENT_TAG)
//                    .commit();
        }

        /*
           No need to add the DetailsFragment if the container is present.
           That is because the fragments within DetailsFragment fetch data
           either from the server or the content provider. In any case, they
           need a Uri to get the data. That Uri can only be provided
           when the movies have been loaded in CatalogFragment.
         */

        mTwoPane = findViewById(R.id.frl_details_container) != null;

        startSpinner();

        /*
            This layout has to be recovered because it is used as the root
            scene for the transitions between catalog and search fragments
         */
        frl_catalog_container = (FrameLayout) findViewById(R.id.frl_catalog_container);

        tbr = (Toolbar) findViewById(R.id.tbr_catalog);
        setSupportActionBar(tbr);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setElevation(0);

        MoviesSyncAdapter.initializeSyncAdapter(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String queryBy = Utility.getPreferredValue(
                this,
                getString(R.string.prf_key_sort),
                getString(R.string.prf_default_sort)
        );

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
            ActivityOptionsCompat activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(this);
            ActivityCompat.startActivity(this, intent, activityOptions.toBundle());
            /*
                TODO:Currently, it's not possible to add an animation to the TabWidget.
                It simply appears before the other elements, and it does not look nice.
             */
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

    private void switchToMode(@AppStatus.MoviesMode int mode) throws Exception{
        int layout = -1;
        String tag = "";
        Class frg = null;
        switch (mode){
            case AppStatus.MOVIES_CATALOG_MODE:
                layout = R.layout.fragment_catalog;
                tag = CATALOG_FRAGMENT_TAG;
                frg = CatalogFragment.class;
                break;
            case AppStatus.MOVIES_SEARCH_MODE:
                layout = R.layout.fragment_search;
                tag = SEARCH_FRAGMENT_TAG;
                frg = SearchFragment.class;
                break;
            default:
                break;
        }

        if(layout == -1){
            throw new IllegalArgumentException("Invalid movie mode");
        }

        Scene newScene = Scene.getSceneForLayout(frl_catalog_container, layout, this);
        Transition tra = TransitionInflater.from(this).inflateTransition(R.transition.transition_catalog_search);
        TransitionManager.go(newScene, tra);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frl_catalog_container, Utility.createFragment(frg), tag)
                .commit();
    }

    private void startSpinner(){
        spr_query_mode = (Spinner) findViewById(R.id.spr_query_mode);
        String modes[] = getResources().getStringArray(R.array.lbls_movies_mode);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, modes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spr_query_mode.setAdapter(adapter);
        @AppStatus.MoviesMode int moviesMode = Utility.getPreferredValue(
                this,
                getString(R.string.pref_movie_mode_key),
                AppStatus.MOVIES_CATALOG_MODE
        );
        spr_query_mode.setSelection(moviesMode);
        spr_query_mode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Utility.setPreferredValue(
                        CatalogActivity.this,
                        CatalogActivity.this.getString(R.string.pref_movie_mode_key),
                        i,
                        true);
                try {
                    switchToMode(i);
                }
                catch (Exception e){
                    Log.e(LOG_TAG, "Not possible to switch to another fragment");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }
}
