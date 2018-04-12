package com.keemsa.tourguide;


import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.keemsa.tourguide.adapter.MuseumAdapter;
import com.keemsa.tourguide.place.Museum;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
@SuppressWarnings("ResourceType")
public class MuseumFragment extends Fragment {

    public MuseumFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.list_places, container, false);

        final ArrayList<Museum> museums = new ArrayList<Museum>();

        for(TypedArray item : ResourceHelper.getMultiTypedArray(getContext(), "museum")){
            Museum museum = new Museum(item.getString(0), item.getString(1), item.getString(2), Double.valueOf(item.getString(3)));
            museums.add(museum);
            String imgName = item.getString(4);
            int imageId = getResources().getIdentifier("@drawable/" + imgName, "drawable", getActivity().getPackageName());
            museum.setImageId(imageId);
        }

        MuseumAdapter adapter = new MuseumAdapter(getActivity(), museums, R.color.place_museum);

        ListView lsv_places = (ListView) rootView.findViewById(R.id.lsv_places);

        lsv_places.setAdapter(adapter);

        return rootView;
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
