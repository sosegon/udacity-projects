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
import com.keemsa.tourguide.place.Park;

import java.util.List;

/**
 * Created by sebastian on 06/07/16.
 */
public class ParkAdapter extends ArrayAdapter<Park> {

    private int mColorResourceId;

    public ParkAdapter(Context context, List<Park> parks, int colorResourceId) {
        super(context, 0, parks);
        this.mColorResourceId = colorResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Check if an existing view is being reused, otherwise inflate the view
        View parkItemView = convertView;
        if(parkItemView == null){
            parkItemView = LayoutInflater.from(getContext()).inflate(R.layout.item_park, parent, false);
        }

        // Get the {@link Park} object located at this position in the list
        final Park currentPark = getItem(position);

        ImageView img_thumbnail = (ImageView) parkItemView.findViewById(R.id.img_thumbnail);
        TextView txt_name = (TextView) parkItemView.findViewById(R.id.txt_name);
        TextView txt_location = (TextView) parkItemView.findViewById(R.id.txt_location);
        TextView txt_facilities = (TextView) parkItemView.findViewById(R.id.txt_facilities);
        View ll_text_container = parkItemView.findViewById(R.id.ll_text_container);

        if(currentPark.hasImage()){
            img_thumbnail.setImageResource(currentPark.getImageId());
            img_thumbnail.setVisibility(ImageView.VISIBLE);
        }
        else{
            img_thumbnail.setVisibility(ImageView.GONE);
        }

        txt_name.setText(currentPark.getName());
        txt_location.setText(R.string.label_view_map);
        txt_facilities.setText(currentPark.getFacilitiesAsString());

        ll_text_container.setBackgroundColor(ContextCompat.getColor(getContext(), mColorResourceId));

        txt_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intMap = new Intent(Intent.ACTION_VIEW, Uri.parse(currentPark.getMapLocation()));
                getContext().startActivity(intMap);
            }
        });

        return parkItemView;
    }
}
