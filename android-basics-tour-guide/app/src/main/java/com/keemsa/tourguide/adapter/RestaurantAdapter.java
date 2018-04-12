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
import com.keemsa.tourguide.place.Restaurant;

import java.util.List;

/**
 * Created by sebastian on 06/07/16.
 */
public class RestaurantAdapter extends ArrayAdapter<Restaurant> {

    private int mColorResourceId;

    public RestaurantAdapter(Context context, List<Restaurant> restaurants, int colorResourceId) {
        super(context, 0, restaurants);
        this.mColorResourceId = colorResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Check if an existing view is being reused, otherwise inflate the view
        View restaurantItemView = convertView;
        if (restaurantItemView == null) {
            restaurantItemView = LayoutInflater.from(getContext()).inflate(R.layout.item_restaurant, parent, false);
        }

        // Get the {@link Restaurant} object located at this position in the list

        final Restaurant currentRestaurant = getItem(position);

        ImageView img_thumbnail = (ImageView) restaurantItemView.findViewById(R.id.img_thumbnail);
        TextView txt_name = (TextView) restaurantItemView.findViewById(R.id.txt_name);
        TextView txt_address = (TextView) restaurantItemView.findViewById(R.id.txt_address);
        TextView txt_service_hours = (TextView) restaurantItemView.findViewById(R.id.txt_service_hours);
        TextView txt_food_type = (TextView) restaurantItemView.findViewById(R.id.txt_food_type);
        View ll_text_container = restaurantItemView.findViewById(R.id.ll_text_container);

        if (currentRestaurant.hasImage()) {
            img_thumbnail.setImageResource(currentRestaurant.getImageId());
            img_thumbnail.setVisibility(ImageView.VISIBLE);
        } else {
            img_thumbnail.setVisibility(ImageView.GONE);
        }

        txt_name.setText(currentRestaurant.getName());
        txt_address.setText(R.string.label_view_map);
        txt_service_hours.setText(currentRestaurant.getServiceHours());
        txt_food_type.setText(currentRestaurant.getFoodType() + " food");

        ll_text_container.setBackgroundColor(ContextCompat.getColor(getContext(), mColorResourceId));

        txt_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intMap = new Intent(Intent.ACTION_VIEW, Uri.parse(currentRestaurant.getAddress()));
                getContext().startActivity(intMap);
            }
        });

        return restaurantItemView;
    }

}
