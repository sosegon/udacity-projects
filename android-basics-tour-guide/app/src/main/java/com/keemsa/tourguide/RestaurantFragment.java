package com.keemsa.tourguide;


import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.keemsa.tourguide.adapter.MuseumAdapter;
import com.keemsa.tourguide.adapter.RestaurantAdapter;
import com.keemsa.tourguide.place.Museum;
import com.keemsa.tourguide.place.Restaurant;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
@SuppressWarnings("ResourceType")
public class RestaurantFragment extends Fragment {

    public RestaurantFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.list_places, container, false);

        final ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();

        for (TypedArray item : ResourceHelper.getMultiTypedArray(getContext(), "restaurant")) {
            Restaurant restaurant = new Restaurant(item.getString(0), item.getString(1), item.getString(2), item.getString(3));
            restaurants.add(restaurant);
            String imgName = item.getString(4);
            int imageId = getResources().getIdentifier("@drawable/" + imgName, "drawable", getActivity().getPackageName());
            restaurant.setImageId(imageId);
        }

        RestaurantAdapter adapter = new RestaurantAdapter(getActivity(), restaurants, R.color.place_restaurant);

        ListView lsv_places = (ListView) rootView.findViewById(R.id.lsv_places);

        lsv_places.setAdapter(adapter);

        return rootView;
    }

    @Override
    public void onStop() {
        super.onStop();
    }

}
