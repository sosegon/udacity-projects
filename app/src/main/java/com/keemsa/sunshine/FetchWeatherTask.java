package com.keemsa.sunshine;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by sebastian on 26/08/16.
 */
public class FetchWeatherTask extends AsyncTask<String, Void, String> {

    public interface FetchWeatherTaskResponse{
        void processWeatherData(String forecastJson);
    }

    private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
    private FetchWeatherTaskResponse response;

    public FetchWeatherTask(FetchWeatherTaskResponse response) {
        this.response = response;
    }

    @Override
    protected void onPostExecute(String s) {
        response.processWeatherData(s);
    }

    @Override
    protected String doInBackground(String... strings) {

        if(strings.length == 0){
            return null;
        }

        final String BASE_URL = "http://api.openweathermap.org/data/2.5/weather?";
        final String QUERY_PARAM = "q";
        final String FORMAT_PARAM = "mode";
        final String UNITS_PARAM = "units";
        final String DAYS_PARAM = "cnt";
        final String APPID_PARAM = "appid";

        //q=quito,ec&amp;cnt=7&amp;units=metric&amp;appid=
        Uri uri = Uri.parse(BASE_URL).buildUpon()
                .appendQueryParameter(QUERY_PARAM, strings[0])
                .appendQueryParameter(FORMAT_PARAM, "json")
                .appendQueryParameter(UNITS_PARAM, "metric")
                .appendQueryParameter(DAYS_PARAM, "7")
                .appendQueryParameter(APPID_PARAM, "22e87de1a93094df1aaa5225efd88348")
                .build();

        return fetchWeatherData(uri);
    }

    private String fetchWeatherData(Uri uri){
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String forecastJSON = null;

        try{
            URL myUrl = new URL(uri.toString());
            urlConnection = (HttpURLConnection) myUrl.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            Log.i(LOG_TAG, "Response is: " + urlConnection.getResponseCode());

            InputStream stream = urlConnection.getInputStream();

            if(stream == null){
                forecastJSON = null;
            }

            reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
            String line;
            StringBuilder output = new StringBuilder();
            while((line = reader.readLine()) != null){
                output.append(line);
            }

            if(output.length() == 0){
                forecastJSON = null;
            }

            forecastJSON = output.toString();

        }
        catch (IOException e){
            forecastJSON = null;
            Log.e(LOG_TAG, "Error");
        }
        finally {
            if(urlConnection != null){
                urlConnection.disconnect();
            }

            if(reader != null){
                try {
                    reader.close();
                }
                catch (IOException e){
                    Log.e(LOG_TAG, "Error closing stream");
                }
            }
        }

        if(forecastJSON != null)
            Log.i(LOG_TAG, forecastJSON); // For debugging purpose only
        else
            Log.i(LOG_TAG, "forecastJSON empty");
        return forecastJSON;
    }
}
