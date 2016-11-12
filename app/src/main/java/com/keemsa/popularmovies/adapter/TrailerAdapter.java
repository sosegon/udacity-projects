package com.keemsa.popularmovies.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.keemsa.popularmovies.R;
import com.keemsa.popularmovies.data.Queries;

/**
 * Created by sebastian on 10/16/16.
 */
public class TrailerAdapter extends CursorAdapter {

    public static final class ViewHolder {
        public final ImageView imv_trailer_thumbnail;
        public final TextView txt_trailer_name;

        public ViewHolder(View view) {
            imv_trailer_thumbnail = (ImageView) view.findViewById(R.id.imv_trailer_thumbnail);
            txt_trailer_name = (TextView) view.findViewById(R.id.txt_trailer_name);
        }
    }

    private String LOG_TAG = TrailerAdapter.class.getSimpleName();
    private ViewHolder holder;

    public TrailerAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        holder.txt_trailer_name.setText(cursor.getString(Queries.TRAILER_NAME));
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_trailer, viewGroup, false);
        holder = new ViewHolder(view);
        return view;
    }
}
