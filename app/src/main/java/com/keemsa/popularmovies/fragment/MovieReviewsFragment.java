package com.keemsa.popularmovies.fragment;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
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
import android.widget.TextView;

import com.keemsa.popularmovies.R;
import com.keemsa.popularmovies.Utility;
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
public class MovieReviewsFragment extends Fragment {

    private final String LOG_TAG = MovieReviewsFragment.class.getSimpleName();

    private ReviewPagerAdapter<ReviewFragment> reviewAdapter;
    private ViewPager pgr_reviews;
    private TextView txt_reviews_msg;

    private Uri mMovieUri;
    private long mMovieId;
    private int mFetchFromServerCount = 0;

    private final int REVIEW_CURSOR_LOADER_ID = 0;
    private final int REVIEW_ASYNC_LOADER_ID = 1;
    final static String[] REVIEW_COLUMNS = {
            ReviewColumns.AUTHOR,
            ReviewColumns.CONTENT,
    };
    final static int REVIEW_AUTHOR = 0;
    final static int REVIEW_CONTENT = 1;

    private LoaderManager.LoaderCallbacks<String> asyncLoader = new LoaderManager.LoaderCallbacks<String>() {
        @Override
        public Loader<String> onCreateLoader(int id, Bundle args) {
            /*
                The app is here because it didn't find trailers with
                the cursor loader. After using this loader, the
                cursor loader will be restarted.
             */
            mFetchFromServerCount++;

            // This happens in tablets
            if (mMovieUri != null) {
                return Utility.getLoaderBasedOnMovieUri(getContext(), ReviewsAsyncTask.class, mMovieUri);
            }

            // This happens in phones
            Intent intent = getActivity().getIntent();
            if (intent == null) {
                return null;
            }

            return Utility.getLoaderBasedOnMovieUri(getContext(), ReviewsAsyncTask.class, mMovieUri);
        }

        @Override
        public void onLoadFinished(Loader<String> loader, String data) {
            processJson(data);
        }

        @Override
        public void onLoaderReset(Loader<String> loader) {

        }
    };

    private LoaderManager.LoaderCallbacks<Cursor> cursorLoader = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            // This happens in tablets
            if (mMovieUri != null) {
                Uri reviewUri = MovieProvider.Review.ofMovie(mMovieId);

                return new CursorLoader(
                        getActivity(),
                        reviewUri,
                        REVIEW_COLUMNS,
                        null,
                        null,
                        null
                );
            }

            // This happens in phones
            Intent intent = getActivity().getIntent();
            if (intent == null) {
                return null;
            }

            Uri reviewUri = MovieProvider.Review.ofMovie(mMovieId);
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
            Log.i(LOG_TAG, "Records for review of movie: " + data.getCount());
            reviewAdapter.swapCursor(data);
            /*
                If there are no reviews for the movie with the cursor loader,
                proceed to fetch reviews using the async loader. After results
                are obtained, the cursor loader has to be restarted, which may
                result in no reviews again, because it was not possible to fetch
                them with the async loader or there are no reviews in the server.
                By checking mFetchFromServerCount, infinite loop is avoided.
            */
            if (data.getCount() == 0 && mFetchFromServerCount == 0) {
                getLoaderManager().initLoader(REVIEW_ASYNC_LOADER_ID, null, asyncLoader);
            }

            if (txt_reviews_msg != null) {
                txt_reviews_msg.setVisibility(data.getCount() == 0 ? View.VISIBLE : View.GONE);
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            reviewAdapter.swapCursor(null);
        }
    };

    public MovieReviewsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // This happens in tablets
        Bundle args = getArguments();
        if (args != null) {
            mMovieUri = args.getParcelable(DetailsFragment.MOVIE_URI);
        }

        // This happens in phones
        Intent intent = getActivity().getIntent();
        if (intent != null) {
            mMovieUri = intent.getData();
        }

        // In either case, the uri is ready
        mMovieId = Utility.getMovieIdFromUri(mMovieUri);

        View view = inflater.inflate(R.layout.fragment_movie_reviews, container, false);

        txt_reviews_msg = (TextView) view.findViewById(R.id.txt_reviews_msg);
        txt_reviews_msg.setText(getString(R.string.msg_no_available, getString(R.string.lbl_reviews).toLowerCase()));

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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(REVIEW_CURSOR_LOADER_ID, null, cursorLoader);
        super.onActivityCreated(savedInstanceState);
    }

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
                getLoaderManager().restartLoader(REVIEW_CURSOR_LOADER_ID, null, cursorLoader);
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error parsing json data of reviews");
        }
    }

    private Vector<ContentValues> processReviews(String json) throws JSONException {
        JSONObject dataJson = new JSONObject(json);
        long movieId = dataJson.getLong("id");
        if (movieId != mMovieId) {
            return new Vector<>();
        }

        JSONArray reviewsJson = dataJson.getJSONArray("results");
        Vector<ContentValues> cvVector = new Vector<>(reviewsJson.length());

        for (int i = 0; i < reviewsJson.length(); i++) {
            JSONObject currentReview = reviewsJson.getJSONObject(i);
            String _id = currentReview.optString("id");
            if (reviewExists(_id)) {
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

    private boolean reviewExists(String reviewId) {
        return Utility.reviewExists(getContext(), reviewId);
    }

    private void removeReviewFragments() {
        List<Fragment> fragments = getFragmentManager().getFragments();
        ArrayList<Fragment> toRemove = new ArrayList<Fragment>();

        for (Fragment frg : fragments) {
            if (frg instanceof ReviewFragment) {
                toRemove.add(frg);
            }
        }

        for (Fragment frg : toRemove) {
            getFragmentManager().beginTransaction().remove(frg).commit();
        }
    }
}
