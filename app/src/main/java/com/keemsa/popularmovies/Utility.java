package com.keemsa.popularmovies;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.keemsa.popularmovies.data.MovieColumns;
import com.keemsa.popularmovies.data.MovieProvider;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by sebastian on 10/4/16.
 */
public final class Utility {

    private static final String LOG_TAG = Utility.class.getSimpleName();

    public static long getDateInMilliSeconds(String date) {

        SimpleDateFormat format = new SimpleDateFormat("yyyy-mm-dd");
        format.setTimeZone(TimeZone.getTimeZone("GMT+0:01")); // one minute ahead
        try {
            Date argDate = format.parse(date);
            return argDate.getTime();
        } catch (ParseException e) {
            Log.e(LOG_TAG, "Error while converting date to milliseconds");
        }

        return -1;
    }

    public static String formatPosterUrl(String posterUrl) {
        if (posterUrl.charAt(0) == '/') {
            return posterUrl.substring(1);
        }
        return posterUrl;
    }

    public static String getPreferredValue(Context context, String key, String defaultValue){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return  pref.getString(key, defaultValue);
    }

    public static void setPreferredValue(Context context, String key, String value, boolean async){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefEditor = pref.edit();
        prefEditor.putString(key, value);

        if(async){
            prefEditor.apply();
        }
        else{
            prefEditor.commit();
        }
    }

    public static Uri createTrailerUri(String site, String key) {
        if (site.toLowerCase().equals("youtube")) {
            return Uri.parse("http://www.youtube.com").buildUpon()
                    .appendPath("watch")
                    .appendQueryParameter("v", key).build();
        } else if (site.toLowerCase().equals("vimeo")) {
            return Uri.parse("http://www.vimeo.com").buildUpon()
                    .appendPath(key).build();
        }

        return null;
    }

    public static boolean movieExists(Context context, long movieId) {
        Cursor c = context.getContentResolver().query(
                MovieProvider.Movie.withId(movieId),
                null,
                null,
                null,
                null
        );

        return c.moveToFirst();
    }

    public static boolean reviewExists(Context context, String reviewId) {
        Cursor c = context.getContentResolver().query(
                MovieProvider.Review.withId(reviewId),
                null,
                null,
                null,
                null
        );

        return c.moveToFirst();
    }

    public static boolean trailerExists(Context context, String trailerId) {
        Cursor c = context.getContentResolver().query(
                MovieProvider.Trailer.withId(trailerId),
                null,
                null,
                null,
                null
        );

        return c.moveToFirst();
    }

    public static boolean isFavourite(int queryType) {
        return queryType == 1 || queryType == 3 || queryType == 5 || queryType == 7;
    }

    public static boolean isPopular(int queryType) {
        return queryType == 2 || queryType == 3 || queryType == 6 || queryType == 7;
    }

    public static boolean isRated(int queryType) {
        return queryType >= 4 && queryType <= 7;
    }

    public static int createQueryType(boolean rated, boolean popular, boolean favourite) {
        String type = "" +
                (rated ? "1" : "0") +
                (popular ? "1" : "0") +
                (favourite ? "1" : "0");

        return Integer.parseInt(type, 2);
    }

    public static boolean[] getValuesFromQueryType(int queryType) {
        return new boolean[]{isRated(queryType), isPopular(queryType), isFavourite(queryType)};
    }

    public static String queryFilterByQueryBy(Context context) {
        String queryBy = Utility.getPreferredValue(
                context,
                context.getString(R.string.prf_key_sort),
                context.getString(R.string.prf_default_sort)
        );

        if (queryBy.equals(context.getResources().getStringArray(R.array.prf_values_sort)[0])) { // popular
            return MovieColumns.QUERY_TYPE + " in (2, 3, 6, 7)";
        } else if (queryBy.equals(context.getResources().getStringArray(R.array.prf_values_sort)[1])) { // rated
            return MovieColumns.QUERY_TYPE + " >= 4";
        } else { // favourite
            return MovieColumns.QUERY_TYPE + " in (3, 5, 7)";
        }
    }

    /*
       Used to set the value of the row MovieColumns.QUERY_TYPE
       This function is used when the movie is first fetched from the
       server (it's not present in the db). In this case, the movie has
       been fetched either by popularity or rating, no other option is
       possible
     */
    public static int queryTypeByQueryBy(Context context) {
        String queryBy = Utility.getPreferredValue(
                context,
                context.getString(R.string.prf_key_sort),
                context.getString(R.string.prf_default_sort)
        );

        if (queryBy.equals(context.getResources().getStringArray(R.array.prf_values_sort)[0])) { // popular
            return 2;
        } else if (queryBy.equals(context.getResources().getStringArray(R.array.prf_values_sort)[1])) { // rated
            return 4;
        } else {
            return -1;
        }
    }

    public static String getPosterDirectory(Context context) {
        return context.getString(R.string.folder_posters);
    }

    /*
       Code from http://www.codexpedia.com/android/android-download-and-save-image-through-picasso/
     */
    public static Target picassoImageTarget(final Context context, final String imageDir, final String imageName, @Nullable final ImageView view) {
        ContextWrapper cw = new ContextWrapper(context);
        final File directory = cw.getDir(imageDir, Context.MODE_PRIVATE);
        return new Target() {
            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final File myImageFile = new File(directory, imageName); // Create image file
                        FileOutputStream fos = null;
                        try {
                            fos = new FileOutputStream(myImageFile);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                fos.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        /*
                            Once the image is in the file, load it to the view
                         */
                        if(view != null) {
                            view.post(new Runnable() {
                                @Override
                                public void run() {
                                    Picasso.with(context).load(myImageFile).fit().placeholder(R.drawable.ic_movie_placeholder).into(view);
                                }
                            });
                        }

                        Log.i(LOG_TAG, "image saved to: " + myImageFile.getAbsolutePath());
                    }
                }).start();
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                if (placeHolderDrawable != null) {
                }
            }
        };
    }

    public static void downloadAndSavePoster(Context context, String posterUrl, ImageView view) {
        String imageDir = Utility.getPosterDirectory(context);
        String fullPosterUrl = Uri.parse(context.getString(R.string.base_img_url)).buildUpon().appendPath(posterUrl).build().toString();

        /*
            Loads the image to a file
         */
        Picasso.with(context).load(fullPosterUrl).into(Utility.picassoImageTarget(context, imageDir, posterUrl, view));
    }

    // taken from http://www.seal.io/2010/12/only-way-how-to-align-text-in-block-in.html
    public static String justifyText(String text) {
        return "<html>" +
                "<body>" +
                "<p align=\"justify\">" +
                text +
                "</p>" +
                "</body>" +
                "</html>";
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = manager.getActiveNetworkInfo();

        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public static Loader<String> getLoaderBasedOnMovieUri(Context context, Class<? extends AsyncTaskLoader> loader, Uri movieUri) {
        long movieId = Long.parseLong(movieUri.getLastPathSegment());
        Class[] args = new Class[2];
        args[0] = Context.class;
        args[1] = long.class;

        try {
            return Utility.isNetworkAvailable(context) ? (Loader<String>) loader.getDeclaredConstructor(args).newInstance(context, movieId) : null;
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        }

        return null;
    }

    public static long getMovieIdFromUri(Uri uri) {
        return Long.parseLong(uri.getLastPathSegment());
    }

    @SuppressWarnings("ResourceType") // It helps to supress the error in line return
    public static
    @AppStatus.MoviesStatus
    int getMoviesStatus(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

        return pref.getInt(context.getString(R.string.pref_movies_status_key), AppStatus.MOVIES_STATUS_UNKNOWN);
    }

    public static void setMoviesStatus(Context context, @AppStatus.MoviesStatus int moviesStatus) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefEditor = pref.edit();
        prefEditor.putInt(context.getString(R.string.pref_movies_status_key), moviesStatus);
        prefEditor.commit(); // use commit since the adapter is in background thread, otherwise use apply
    }

    public static void updateMoviesEmptyView(Context context, TextView txt_message) {
        String message = context.getString(R.string.msg_data_no_available, context.getString(R.string.lbl_movies).toLowerCase());

        @AppStatus.MoviesStatus int status = Utility.getMoviesStatus(context);

        switch (status) {
            case AppStatus.MOVIES_STATUS_SERVER_DOWN:
                message = context.getString(R.string.msg_data_no_available_server_down, context.getString(R.string.lbl_movies).toLowerCase());
                break;
            case AppStatus.MOVIES_STATUS_SERVER_INVALID:
                message = context.getString(R.string.msg_data_no_available_server_error, context.getString(R.string.lbl_movies).toLowerCase());
                break;
            default:
                if (!Utility.isNetworkAvailable(context)) {
                    message = context.getString(R.string.msg_data_no_available_no_network, context.getString(R.string.lbl_movies).toLowerCase());
                }
        }

        txt_message.setText(message);
    }

    public static void goLoader(Fragment fragment, int loaderId, LoaderManager.LoaderCallbacks callbacks) {

        /*
            Check the existence of the fragment to avoid creating a new one,
            and just restart the previous one.
         */
        if (fragment.getLoaderManager().getLoader(loaderId) == null) {
            fragment.getLoaderManager().initLoader(loaderId, null, callbacks);
        } else {
            fragment.getLoaderManager().restartLoader(loaderId, null, callbacks);
        }
    }
}
