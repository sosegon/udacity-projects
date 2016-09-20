package com.keemsa.sunshine;


import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.keemsa.sunshine.data.WeatherContract;

/**
 * A simple {@link Fragment} subclass.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private TextView
            txt_detail_day,
            txt_detail_date,
            txt_detail_max_temp,
            txt_detail_min_temp,
            txt_detail_humidity,
            txt_detail_wind,
            txt_detail_pressure,
            txt_detail_forecast;
    private ImageView imv_detail_icon;
    private String sForecast;
    private Uri mUri;
    static final String DETAIL_URI = "URI";
    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";

    private ShareActionProvider mShareActionProvider;

    private static final int WEATHER_WITH_LOCATION_AND_DATE_LOADER = 1;
    private static final String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID

    };

    private static final int
            COL_WEATHER_ID = 0,
            COL_DATE = 1,
            COL_SHORT_DESC = 2,
            COL_MIN_TEMP = 3,
            COL_MAX_TEMP = 4,
            COL_HUMIDITY = 5,
            COL_WIND_SPEED = 6,
            COL_DEGREES = 7,
            COL_PRESSURE = 8,
            COL_WEATHER_CONDITION_ID = 9;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
        }

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_detail, container, false);

        txt_detail_day = (TextView) view.findViewById(R.id.txt_detail_day);
        txt_detail_date = (TextView) view.findViewById(R.id.txt_detail_date);
        txt_detail_max_temp = (TextView) view.findViewById(R.id.txt_detail_max_temp);
        txt_detail_min_temp = (TextView) view.findViewById(R.id.txt_detail_min_temp);
        txt_detail_humidity = (TextView) view.findViewById(R.id.txt_detail_humidity);
        txt_detail_wind = (TextView) view.findViewById(R.id.txt_detail_wind);
        txt_detail_pressure = (TextView) view.findViewById(R.id.txt_detail_pressure);
        txt_detail_forecast = (TextView) view.findViewById(R.id.txt_detail_forecast);
        imv_detail_icon = (ImageView) view.findViewById(R.id.imv_detail_icon);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(WEATHER_WITH_LOCATION_AND_DATE_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        if (mUri == null) {
            return null;
        }
        return new CursorLoader(
                getContext(),
                mUri,
                FORECAST_COLUMNS,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()) {
            int weatherId = data.getInt(COL_WEATHER_CONDITION_ID);
            imv_detail_icon.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));

            long date = data.getLong(COL_DATE);
            String friendlyDateText = Utility.getDayName(getContext(), date);
            String dateText = Utility.getFormattedMonthDay(getContext(), date);
            txt_detail_date.setText(friendlyDateText);
            txt_detail_day.setText(dateText);

            // Read description from cursor and update view
            String description = data.getString(COL_SHORT_DESC);
            txt_detail_forecast.setText(description);

            boolean isMetric = Utility.isMetric(getActivity());

            String sDate = Utility.formatDate(data.getLong(COL_DATE));

            String sHigh = Utility.formatTemperature(getContext(), data.getDouble(COL_MAX_TEMP), isMetric);
            txt_detail_max_temp.setText(sHigh);

            String sLow = Utility.formatTemperature(getContext(), data.getDouble(COL_MIN_TEMP), isMetric);
            txt_detail_min_temp.setText(sLow);

            double humidity = data.getDouble(COL_HUMIDITY);
            txt_detail_humidity.setText(getString(R.string.format_humidity, humidity));

            double wind = data.getDouble(COL_WIND_SPEED);
            double degrees = data.getDouble(COL_DEGREES);
            String direction = Utility.getFormattedWind(getContext(), (float) wind, (float) degrees);
            txt_detail_wind.setText(direction);

            double pressure = data.getDouble(COL_PRESSURE);
            txt_detail_pressure.setText(getString(R.string.format_pressure, pressure));

            String sDesc = data.getString(COL_SHORT_DESC);
            txt_detail_forecast.setText(sDesc);


            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareForecastIntent());
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate menu
        inflater.inflate(R.menu.detailfragment, menu);

        MenuItem menuItem = menu.findItem(R.id.mit_share);

        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        if (sForecast != null && mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mit_share:
                startActivity(createShareForecastIntent());
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    void onLocationChanged( String newLocation ) {
        // replace the uri, since the location has changed
        Uri uri = mUri;
        if (null != uri) {
            long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
            Uri updatedUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation, date);
            mUri = updatedUri;
            getLoaderManager().restartLoader(WEATHER_WITH_LOCATION_AND_DATE_LOADER, null, this);
        }
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, sForecast + FORECAST_SHARE_HASHTAG);
        return shareIntent;
    }
}


