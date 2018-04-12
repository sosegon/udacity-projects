package com.keemsa.tourguide;


import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.keemsa.tourguide.adapter.ParkAdapter;
import com.keemsa.tourguide.adapter.RestaurantAdapter;
import com.keemsa.tourguide.place.Park;
import com.keemsa.tourguide.place.Restaurant;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
@SuppressWarnings("ResourceType")
public class ParkFragment extends Fragment {

    public ParkFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.list_places, container, false);

        final ArrayList<Park> parks = new ArrayList<Park>();

        for(TypedArray item : ResourceHelper.getMultiTypedArray(getContext(), "park")){
            Park park = new Park(item.getString(0), item.getString(1), item.getString(2));
            parks.add(park);
            String imgName = item.getString(3);
            int imageId = getResources().getIdentifier("@drawable/" + imgName, "drawable", getActivity().getPackageName());
            park.setImageId(imageId);
        }

        ParkAdapter adapter = new ParkAdapter(getActivity(), parks, R.color.place_park);

        ListView lsv_places = (ListView) rootView.findViewById(R.id.lsv_places);

        lsv_places.setAdapter(adapter);

        return rootView;
    }

    @Override
    public void onStop() {
        super.onStop();
    }

}
