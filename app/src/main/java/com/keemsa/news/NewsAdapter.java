package com.keemsa.news;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by sebastian on 08/07/16.
 */
public class NewsAdapter extends RecyclerView.Adapter<NewsViewHolder> {

    private List<News> newsDataset;
    private Context mContext;

    public NewsAdapter(Context mContext, List<News> newsDataset) {
        this.newsDataset = newsDataset;
        this.mContext = mContext;
    }

    @Override
    public NewsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_news, null);

        NewsViewHolder viewHolder = new NewsViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(NewsViewHolder holder, int position) {
        final News newsItem = newsDataset.get(position);

        if (newsItem.getThumbnailUrl().length() > 0) {
            DownloadThumbnailTask task = new DownloadThumbnailTask(holder.img_thumbnail);
            task.execute(newsItem.getThumbnailUrl());
        }

        holder.txt_headline.setText(Html.fromHtml(newsItem.getHeadline()));
        holder.txt_type.setText(Html.fromHtml(newsItem.getType()));
        holder.img_thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intBrowser = new Intent(Intent.ACTION_VIEW, Uri.parse(newsItem.getWebUrl()));
                mContext.startActivity(intBrowser);
            }
        });
    }

    @Override
    public int getItemCount() {
        return (null != newsDataset ? newsDataset.size() : 0);
    }
}
