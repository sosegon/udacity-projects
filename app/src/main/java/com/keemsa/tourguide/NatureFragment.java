package com.keemsa.tourguide;


import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.keemsa.tourguide.adapter.NatureAdapter;
import com.keemsa.tourguide.adapter.ParkAdapter;
import com.keemsa.tourguide.place.Nature;
import com.keemsa.tourguide.place.Park;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
@SuppressWarnings("ResourceType")
public class NatureFragment extends Fragment {

    public NatureFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.list_places, container, false);

        final ArrayList<Nature> natures = new ArrayList<Nature>();

        for (TypedArray item : ResourceHelper.getMultiTypedArray(getContext(), "nature")) {
            Nature nature = new Nature(item.getString(0), item.getString(1), item.getString(2));
            natures.add(nature);
            String imgName = item.getString(3);
            int imageId = getResources().getIdentifier("@drawable/" + imgName, "drawable", getActivity().getPackageName());
            nature.setImageId(imageId);
        }

        NatureAdapter adapter = new NatureAdapter(getActivity(), natures, R.color.place_nature);

        ListView lsv_places = (ListView) rootView.findViewById(R.id.lsv_places);

        lsv_places.setAdapter(adapter);

        return rootView;
    }

    @Override
    public void onStop() {
        super.onStop();
    }

}
