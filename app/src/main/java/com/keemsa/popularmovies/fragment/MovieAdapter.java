package com.keemsa.popularmovies.fragment;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import com.keemsa.popularmovies.R;
import com.keemsa.popularmovies.data.MovieColumns;
import com.squareup.picasso.Picasso;

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
        int posterUrlIndex = cursor.getColumnIndex(MovieColumns.POSTER_URL);
        String posterUrl = cursor.getString(posterUrlIndex);
        String fullPosterUrl = Uri.parse(context.getString(R.string.base_img_url)).buildUpon().appendPath(posterUrl).build().toString();
        Picasso.with(context).load(fullPosterUrl).into(holder.posterView);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_movie, viewGroup, false);
        holder = new ViewHolder(view);
        return view;
    }
}
