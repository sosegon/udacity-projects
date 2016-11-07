package com.keemsa.popularmovies.adapter;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import com.keemsa.popularmovies.R;
import com.keemsa.popularmovies.Utility;
import com.keemsa.popularmovies.fragment.CatalogFragment;
import com.squareup.picasso.Picasso;

import java.io.File;

/**
 * Created by sebastian on 31/08/16.
 */
public class MovieAdapter extends CursorAdapter {

    public static class ViewHolder {
        public final ImageView posterView;

        public ViewHolder(View view){
            posterView = (ImageView) view.findViewById(R.id.imv_movie_poster);
        }
    }

    private String LOG_TAG = MovieAdapter.class.getSimpleName();
    private ViewHolder holder;

    public MovieAdapter(Context context, Cursor c, int flag) {
        super(context, c, flag);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
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
        ContextWrapper cw = new ContextWrapper(context);
        File directory = cw.getDir(Utility.getPosterDirectory(context), Context.MODE_PRIVATE);
        File posterFile = new File(directory, posterUrl);
        if (posterFile.exists()) {
            Picasso.with(context).load(posterFile).fit().into(holder.posterView);
        }
        else {
            Utility.downloadAndSavePoster(context, posterUrl);
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_movie, viewGroup, false);
        holder = new ViewHolder(view);
        return view;
    }
}
