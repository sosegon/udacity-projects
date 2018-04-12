package com.keemsa.tourguide.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.keemsa.tourguide.R;
import com.keemsa.tourguide.place.Museum;

import java.util.List;

/**
 * Created by sebastian on 06/07/16.
 */
public class MuseumAdapter extends ArrayAdapter<Museum> {

    private int mColorResourceId;

    public MuseumAdapter(Context context, List<Museum> museums, int colorResourceId) {
        super(context, 0, museums);
        this.mColorResourceId = colorResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Check if an existing view is being reused, otherwise inflate the view
        View museumItemView = convertView;
        if (museumItemView == null) {
            museumItemView = LayoutInflater.from(getContext()).inflate(R.layout.item_museum, parent, false);
        }

        // Get the {@link Museum} object located at this position in the list
        final Museum currentMuseum = getItem(position);

        ImageView img_thumbnail = (ImageView) museumItemView.findViewById(R.id.img_thumbnail);
        TextView txt_name = (TextView) museumItemView.findViewById(R.id.txt_name);
        TextView txt_address = (TextView) museumItemView.findViewById(R.id.txt_address);
        TextView txt_service_hours = (TextView) museumItemView.findViewById(R.id.txt_service_hours);
        TextView txt_price = (TextView) museumItemView.findViewById(R.id.txt_price);
        View ll_text_container = museumItemView.findViewById(R.id.ll_text_container);

        if (currentMuseum.hasImage()) {
            img_thumbnail.setImageResource(currentMuseum.getImageId());
            img_thumbnail.setVisibility(ImageView.VISIBLE);
        } else {
            img_thumbnail.setVisibility(ImageView.GONE);
        }

        txt_name.setText(currentMuseum.getName());
        txt_address.setText(R.string.label_view_map);
        txt_service_hours.setText(currentMuseum.getServiceHours());
        txt_price.setText("Price: " + String.valueOf(currentMuseum.getPrice()) + " USD");

        ll_text_container.setBackgroundColor(ContextCompat.getColor(getContext(), mColorResourceId));

        txt_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intMap = new Intent(Intent.ACTION_VIEW, Uri.parse(currentMuseum.getAddress()));
                getContext().startActivity(intMap);
            }
        });

        return museumItemView;
    }
}
