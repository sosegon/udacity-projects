package com.keemsa.popularmovies.fragment;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.keemsa.popularmovies.BuildConfig;
import com.keemsa.popularmovies.R;
import com.keemsa.popularmovies.data.MovieProvider;
import com.keemsa.popularmovies.data.ReviewColumns;
import com.keemsa.popularmovies.net.ReviewsAsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created by sebastian on 10/13/16.
 */
public class MovieReviewsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, ReviewsAsyncTask.AsyncTaskReceiver {

    private final String LOG_TAG = MovieReviewsFragment.class.getSimpleName();

    private ReviewPagerAdapter<ReviewFragment> reviewAdapter;
    private ViewPager pgr_reviews;

    private long mMovieId;
    private int mFetchFromServerCount = 0;

    private final int REVIEW_LOADER = 0;
    final static String[] REVIEW_COLUMNS = {
            ReviewColumns.AUTHOR,
            ReviewColumns.CONTENT,
    };
    final static int REVIEW_AUTHOR = 0;
    final static int REVIEW_CONTENT = 1;

    public MovieReviewsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movie_reviews, container, false);

        /* TODO: Find a better solution to handle ReviewFragments
           I remove ReviewFragment instances when another
           Fragment is displayed in the activity via the FragmentTabHost
           I feel this is not an efficient solution but it works.
           What else can I do?
           The reason to remove those ReviewFragments is because
           when switch to another fragment within the FragmentTabHost
           and coming back to this fragment nothing is displayed in the
           screen even though the ReviewFragments still exist.
           Also, when rotating screen, the apps shows the first review
           rather than the review displayed previously,
           How can I fix that?
         */
        removeReviewFragments();

        pgr_reviews = (ViewPager) view.findViewById(R.id.pgr_reviews);
        reviewAdapter = new ReviewPagerAdapter<>(getFragmentManager(), ReviewFragment.class, REVIEW_COLUMNS, null);
        pgr_reviews.setAdapter(reviewAdapter);

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFetchFromServerCount = 0;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Intent intent = getActivity().getIntent();
        if (intent == null) {
            return null;
        }

        long movieId = Long.parseLong(intent.getData().getLastPathSegment());
        Uri reviewUri = MovieProvider.Review.ofMovie(movieId);
        mMovieId = movieId;
        Log.e("", "Uri is: " + reviewUri.toString());
        return new CursorLoader(
                getActivity(),
                reviewUri,
                REVIEW_COLUMNS,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.e(LOG_TAG, "Records for review of movie: " + data.getCount());
        reviewAdapter.swapCursor(data);
        /*
           If there are no reviews for the movie in the provider,
           proceed to fetch reviews from the server. After results
           are obtained, the loader has to be restarted, which may
           result in no reviews again, because it was not possible
           to fetch reviews or there are no reviews in the server.
           By checking mFetchFromServerCount, infinite loop is avoided.
         */
        if(data.getCount() == 0 && mFetchFromServerCount == 0){
            fetchReviews();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        reviewAdapter.swapCursor(null);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(REVIEW_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void processJson(String json) {
        if (json == null || json.length() == 0) {
            return;
        }

        try {
            Vector<ContentValues> cvReviews = processReviews(json);
            if (cvReviews.size() > 0) {
                ContentValues[] cvArray = new ContentValues[cvReviews.size()];
                cvReviews.toArray(cvArray);
                getContext().getContentResolver().bulkInsert(MovieProvider.Review.ALL, cvArray);
                getLoaderManager().restartLoader(REVIEW_LOADER, null, this);
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error parsing json");
        }
    }

    private void fetchReviews(){
        // Verify network connection to fetch reviews
        ConnectivityManager manager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            getReviewsFromServer();
        }
    }

    private void getReviewsFromServer(){
        // Construct url
        String baseUrl = getString(R.string.base_query_url);
        String url = Uri.parse(baseUrl).buildUpon()
                .appendPath("" + mMovieId)
                .appendPath("reviews")
                .appendQueryParameter("api_key", BuildConfig.MOVIEDB_API_KEY)
                .build()
                .toString();
        ReviewsAsyncTask task = new ReviewsAsyncTask(this);
        task.execute(url);
        mFetchFromServerCount++;
    }

    private Vector<ContentValues> processReviews(String json) throws JSONException {
        JSONObject dataJson = new JSONObject(json);
        long movieId = dataJson.getLong("id");
        JSONArray reviewsJson = dataJson.getJSONArray("results");
        Vector<ContentValues> cvVector = new Vector<>(reviewsJson.length());

        for (int i = 0; i < reviewsJson.length(); i++) {
            JSONObject currentReview = reviewsJson.getJSONObject(i);
            String _id = currentReview.optString("id");
            if(reviewExists(_id)){
                continue;
            }
            String author = currentReview.optString("author"),
                    content = currentReview.optString("content"),
                    url = currentReview.optString("url");

            ContentValues cvReview = new ContentValues();
            cvReview.put(ReviewColumns.AUTHOR, author);
            cvReview.put(ReviewColumns.CONTENT, content);
            cvReview.put(ReviewColumns.URL, url);
            cvReview.put(ReviewColumns._ID, _id);
            cvReview.put(ReviewColumns.MOVIE_ID, movieId);

            cvVector.add(cvReview);
        }

        return cvVector;
    }

    private boolean reviewExists(String reviewId){
        Cursor c = getContext().getContentResolver().query(
                MovieProvider.Review.withId(reviewId),
                null,
                null,
                null,
                null
        );

        return c.moveToFirst();
    }

    private void removeReviewFragments(){
        List<Fragment> fragments = getFragmentManager().getFragments();
        ArrayList<Fragment> toRemove = new ArrayList<Fragment>();

        for(Fragment frg : fragments){
            if(frg instanceof ReviewFragment){
                toRemove.add(frg);
            }
        }

        for(Fragment frg : toRemove){
            getFragmentManager().beginTransaction().remove(frg).commit();
        }
    }
}
