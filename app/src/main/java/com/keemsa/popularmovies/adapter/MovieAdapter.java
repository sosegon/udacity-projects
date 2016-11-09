package com.keemsa.popularmovies.adapter;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.keemsa.popularmovies.R;
import com.keemsa.popularmovies.Utility;
import com.keemsa.popularmovies.fragment.CatalogFragment;
import com.squareup.picasso.Picasso;

import java.io.File;

/**
 * Created by sebastian on 31/08/16.
 */
public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolder> {

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView posterView;

        public ViewHolder(View view) {
            super(view);
            posterView = (ImageView) view.findViewById(R.id.imv_movie_poster);
        }
    }

    private String LOG_TAG = MovieAdapter.class.getSimpleName();
    private ViewHolder holder;
    private final Context mContext;
    private Cursor mCursor;

    public MovieAdapter(Context context) {
        mContext = context;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        mCursor.moveToPosition(position);
        String posterUrl = mCursor.getString(CatalogFragment.MOVIE_POSTER_URL);

        /*
           TODO: Solve the problem of smooth scrolling
           The goal of the next code is to avoid fetching the image from the server every time.
           Instead, the poster is downloaded once and stored in the device, then that image
           will be used in the future.


           The motivation for this feature was to avoid the problem in the GridView that causes
           an incorrect poster displayed for every movie. I thought this was caused because the
           image is fetched from the server every time, but the problem persists when the posters
           are loaded locally.

           After testing the GridView with text only the problem remains. I discovered that the
           problem is not related to images but to the GridView itself.

           I haven't found a solution for that.
         */
        ContextWrapper cw = new ContextWrapper(mContext);
        File directory = cw.getDir(Utility.getPosterDirectory(mContext), Context.MODE_PRIVATE);
        File posterFile = new File(directory, posterUrl);
        if (posterFile.exists()) {
            Picasso.with(mContext).load(posterFile).fit().into(holder.posterView);
        } else {
            Utility.downloadAndSavePoster(mContext, posterUrl);
        }
        try {
            Log.e(LOG_TAG, posterFile.getCanonicalPath().toString());
            Log.e(LOG_TAG, mCursor.getString(CatalogFragment.MOVIE_TITLE));
        } catch (Exception e) {

        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (parent instanceof RecyclerView) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie, parent, false);
            holder = new ViewHolder(view);
            return holder;
        } else {
            throw new RuntimeException("Not bound to RecyclerViewSelection");
        }
    }

    @Override
    public int getItemCount() {
        if (mCursor == null) {
            return 0;
        }

        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    public Cursor getCursor() {
        return mCursor;
    }
}
