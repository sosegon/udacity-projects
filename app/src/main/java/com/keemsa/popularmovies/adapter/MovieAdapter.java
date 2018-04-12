package com.keemsa.popularmovies.adapter;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.keemsa.popularmovies.R;
import com.keemsa.popularmovies.Utility;
import com.keemsa.popularmovies.data.Queries;
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
        public TextView titleView;

        public ViewHolder(View view) {
            super(view);
            posterView = (ImageView) view.findViewById(R.id.imv_movie_poster);
            titleView = (TextView) view.findViewById(R.id.txt_movie_title);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            mCursor.moveToPosition(position);
            mClickHandler.onClick(mCursor.getLong(Queries.MOVIE_ID), this);
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

        String title = mCursor.getString(Queries.MOVIE_TITLE);
        holder.titleView.setText(title);

        String posterUrl = mCursor.getString(Queries.MOVIE_POSTER_URL);
        if (posterUrl == null) {
            return;
        }

        ContextWrapper cw = new ContextWrapper(mContext);
        File directory = cw.getDir(Utility.getPosterDirectory(mContext), Context.MODE_PRIVATE);
        File posterFile = new File(directory, posterUrl);
        if (posterFile.exists()) {
            Picasso.with(mContext).load(posterFile).fit().placeholder(R.drawable.ic_movie_placeholder).into(holder.posterView);
        } else {
            Utility.downloadAndSavePoster(mContext, posterUrl, holder.posterView);
        }

        /*
            The name of the transition has to be set in the source and the destination of the shared
            element. In the case of the destination, that is done in the xml file (see fragment_movie_details.xml)
            The source is every element in the recycler view. Since the name has to be unique, the
            position of the element is added to the general name to accomplish that
         */
        ViewCompat.setTransitionName(holder.posterView, "posterView" + position);
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
