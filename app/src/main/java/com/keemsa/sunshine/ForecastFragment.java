package com.keemsa.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.keemsa.sunshine.data.WeatherContract;

public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private final String LOG_TAG = ForecastFragment.class.getSimpleName();
    private String mLocation;
    private ForecastAdapter adapter;

    private static final int WEATHER_LOADER_ID = 1;

    public ForecastFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_forecast, container, false);

        // The layout has to be inflated before searching for elements on it
        adapter = new ForecastAdapter(getContext(), null, 0);
        final ListView lv = (ListView) view.findViewById(R.id.listview_forecast);
        lv.setAdapter(adapter);

        // Init loader
        getLoaderManager().initLoader(WEATHER_LOADER_ID, null, this);

        // Add listener to detailed view
        final String locationSetting = Utility.getPreferredLocation(getContext());
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Cursor cursor = (Cursor) lv.getItemAtPosition(i);
                Intent intent = new Intent(getContext(), DetailActivity.class);
                intent.setData(
                        WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                                locationSetting,
                                cursor.getLong(ForecastAdapter.COL_WEATHER_DATE)
                        )
                );
                startActivity(intent);
            }
        });

        // set mLocation
        mLocation = Utility.getPreferredLocation(getContext());

        return view;
    }

    public void onLocationChanged(){
        updateWeather();
        getLoaderManager().restartLoader(WEATHER_LOADER_ID, null, this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(WEATHER_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String locationSetting = Utility.getPreferredLocation(getContext());
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        return new CursorLoader(
                getContext(),
                WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(locationSetting, System.currentTimeMillis()),
                ForecastAdapter.FORECAST_COLUMNS,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate menu
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public void onResume() {
        super.onResume();

        String location = Utility.getPreferredLocation(getContext());
        if(location != null && !mLocation.equals(location)){
            onLocationChanged();
            mLocation = location;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mit_refresh:
                updateWeather();
                return true;
            case R.id.mit_settings:
                Intent intent = new Intent(getContext(), SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.mit_location:
                openPreferredLocationMap();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void openPreferredLocationMap() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
        String location = pref.getString(getString(R.string.prf_location_key), getString(R.string.prf_location_default));

        Uri geolocation = Uri.parse("geo:0,0?").buildUpon()
                .appendQueryParameter("q", location)
                .build();

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geolocation);

        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Log.d(LOG_TAG, "Could not call " + location + ", no receiving apps installed");
        }
    }

    private void updateWeather() {
        FetchWeatherTask task = new FetchWeatherTask(this.getContext());
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
        task.execute(pref.getString(getString(R.string.prf_location_key), getString(R.string.prf_location_default)));
    }
}
