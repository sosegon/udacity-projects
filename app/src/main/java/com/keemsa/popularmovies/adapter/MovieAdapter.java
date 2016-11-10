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

    public static interface MovieAdapterOnClickHandler {
        void onClick(long movieId, ViewHolder vh);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView posterView;

        public ViewHolder(View view) {
            super(view);
            posterView = (ImageView) view.findViewById(R.id.imv_movie_poster);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            mCursor.moveToPosition(position);
            mClickHandler.onClick(mCursor.getLong(CatalogFragment.MOVIE_ID), this);
        }
    }

    private String LOG_TAG = MovieAdapter.class.getSimpleName();
    private ViewHolder holder;
    private final Context mContext;
    private Cursor mCursor;
    private final MovieAdapterOnClickHandler mClickHandler;
    private final View mEmptyView;

    public MovieAdapter(Context context, MovieAdapterOnClickHandler ch, View emptyView) {
        mClickHandler = ch;
        mEmptyView = emptyView;
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
            Picasso.with(mContext).load(posterFile).noFade().fit().into(holder.posterView);
        } else {
            Utility.downloadAndSavePoster(mContext, posterUrl);
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
        mEmptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    public Cursor getCursor() {
        return mCursor;
    }
}
