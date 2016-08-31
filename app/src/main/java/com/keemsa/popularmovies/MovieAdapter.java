package com.keemsa.popularmovies;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.keemsa.popularmovies.model.Movie;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by sebastian on 31/08/16.
 */
public class MovieAdapter extends ArrayAdapter<Movie> {

    private String LOG_TAG = MovieAdapter.class.getSimpleName();

    public MovieAdapter(Context context, int resource, List<Movie> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Movie movie = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_movie, parent, false);
        }

        ImageView imv_movie_poster = (ImageView) convertView.findViewById(R.id.imv_movie_poster);

        Picasso.with(getContext()).load(movie.getPosterUrl()).into(imv_movie_poster);

        return convertView;
    }
}
