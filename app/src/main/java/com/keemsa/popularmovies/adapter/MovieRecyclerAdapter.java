package com.keemsa.popularmovies.adapter;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.keemsa.popularmovies.R;
import com.keemsa.popularmovies.Utility;
import com.keemsa.popularmovies.fragment.CatalogFragment;

import java.io.File;

/**
 * Created by sebastian on 11/3/16.
 */
public class MovieRecyclerAdapter extends CursorRecyclerViewAdapter<MovieRecyclerAdapter.ViewHolder> {

    private ViewHolder holder;

    public MovieRecyclerAdapter(Context context, Cursor cursor){
        super(context, cursor);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView posterView;
        public ViewHolder(View view) {
            super(view);
            posterView = (ImageView) view.findViewById(R.id.imv_movie_poster);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie, parent, false);
        holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Cursor cursor) {
        String posterUrl = cursor.getString(CatalogFragment.MOVIE_POSTER_URL);

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
            Glide.with(mContext).load(posterFile).into(holder.posterView);
            //Picasso.with(context).load(posterFile).fit().into(holder.posterView);
        }
        else {
            Utility.downloadAndSavePoster(mContext, posterUrl);
        }
    }
}
