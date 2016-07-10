package com.keemsa.news;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by sebastian on 09/07/16.
 */
public class NewsViewHolder extends RecyclerView.ViewHolder {

    protected TextView txt_headline;
    protected TextView txt_type;
    protected ImageView img_thumbnail;

    public NewsViewHolder(View itemView) {
        super(itemView);
        this.txt_headline = (TextView) itemView.findViewById(R.id.txt_headline);
        this.txt_type = (TextView) itemView.findViewById(R.id.txt_type);
        this.img_thumbnail = (ImageView) itemView.findViewById(R.id.img_thumbnail);
    }
}
