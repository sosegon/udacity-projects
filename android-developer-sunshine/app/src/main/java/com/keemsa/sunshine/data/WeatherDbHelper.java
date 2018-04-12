package com.keemsa.sunshine.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by sebastian on 07/09/16.
 */
public class WeatherDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "weather.db";

    public WeatherDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_LOCATION_TABLE = "create table " + WeatherContract.LocationEntry.TABLE_NAME + " (" +
                WeatherContract.LocationEntry._ID + " integer primary key autoincrement, " +

                WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " text unique not null, " +
                WeatherContract.LocationEntry.COLUMN_COORD_LAT + " real not null, " +
                WeatherContract.LocationEntry.COLUMN_COORD_LONG + " real not null, " +
                WeatherContract.LocationEntry.COLUMN_CITY_NAME + " text not null)";

        final String SQL_CREATE_WEATHER_TABLE = "create table " + WeatherContract.WeatherEntry.TABLE_NAME + " (" +
                WeatherContract.WeatherEntry._ID + " integer primary key autoincrement, " +

                WeatherContract.WeatherEntry.COLUMN_LOC_KEY + " integer not null, " +
                WeatherContract.WeatherEntry.COLUMN_DATE + " integer not null, " +
                WeatherContract.WeatherEntry.COLUMN_SHORT_DESC + " text not null, " +
                WeatherContract.WeatherEntry.COLUMN_WEATHER_ID + " integer not null, " +

                WeatherContract.WeatherEntry.COLUMN_MIN_TEMP + " real not null, " +
                WeatherContract.WeatherEntry.COLUMN_MAX_TEMP + " real not null, " +

                WeatherContract.WeatherEntry.COLUMN_HUMIDITY + " real not null, " +
                WeatherContract.WeatherEntry.COLUMN_PRESSURE + " real not null, " +
                WeatherContract.WeatherEntry.COLUMN_WIND_SPEED + " real not null, " +
                WeatherContract.WeatherEntry.COLUMN_DEGREES + " real not null, " +

                " foreign key (" + WeatherContract.WeatherEntry.COLUMN_LOC_KEY + ") references " +
                WeatherContract.LocationEntry.TABLE_NAME + " (" + WeatherContract.LocationEntry._ID + "), " +

                " unique (" + WeatherContract.WeatherEntry.COLUMN_DATE + ", " +
                WeatherContract.WeatherEntry.COLUMN_LOC_KEY + ") on conflict replace);";

        sqLiteDatabase.execSQL(SQL_CREATE_LOCATION_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_WEATHER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        // Drop the tables because the data is not created by the user
        // It comes from openweathermap.org
        sqLiteDatabase.execSQL("drop table if exists " + WeatherContract.LocationEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("drop table if exists " + WeatherContract.WeatherEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}

