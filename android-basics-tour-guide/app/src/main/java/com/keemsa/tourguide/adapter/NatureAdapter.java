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
import com.keemsa.tourguide.place.Nature;

import java.util.List;

/**
 * Created by sebastian on 06/07/16.
 */
public class NatureAdapter extends ArrayAdapter<Nature> {

    private int mColorResourceId;

    public NatureAdapter(Context context, List<Nature> natures, int colorResourceId) {
        super(context, 0, natures);
        this.mColorResourceId = colorResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Check if an existing view is being reused, otherwise inflate the view
        View natureItemView = convertView;
        if (natureItemView == null) {
            natureItemView = LayoutInflater.from(getContext()).inflate(R.layout.item_nature, parent, false);
        }

        // Get the {@link Nature} object located at this position in the list
        final Nature currentNature = getItem(position);

        ImageView img_thumbnail = (ImageView) natureItemView.findViewById(R.id.img_thumbnail);
        TextView txt_name = (TextView) natureItemView.findViewById(R.id.txt_name);
        TextView txt_location = (TextView) natureItemView.findViewById(R.id.txt_location);
        TextView txt_attractions = (TextView) natureItemView.findViewById(R.id.txt_attractions);
        View ll_text_container = natureItemView.findViewById(R.id.ll_text_container);

        if (currentNature.hasImage()) {
            img_thumbnail.setImageResource(currentNature.getImageId());
            img_thumbnail.setVisibility(ImageView.VISIBLE);
        } else {
            img_thumbnail.setVisibility(ImageView.GONE);
        }

        txt_name.setText(currentNature.getName());
        txt_location.setText(R.string.label_view_map);
        txt_attractions.setText(currentNature.getAttractionsAsString());

        ll_text_container.setBackgroundColor(ContextCompat.getColor(getContext(), mColorResourceId));

        txt_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intMap = new Intent(Intent.ACTION_VIEW, Uri.parse(currentNature.getMapLocation()));
                getContext().startActivity(intMap);
            }
        });

        return natureItemView;
    }
}
