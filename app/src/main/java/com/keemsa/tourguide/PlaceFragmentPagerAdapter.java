package com.keemsa.tourguide;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by sebastian on 05/07/16.
 */
public class PlaceFragmentPagerAdapter extends FragmentPagerAdapter {

    private Context mContext;

    public PlaceFragmentPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return new MuseumFragment();
        } else if (position == 1) {
            return new RestaurantFragment();
        } else if (position == 2) {
            return new ParkFragment();
        } else {
            return new NatureFragment();
        }
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) {
            return mContext.getString(R.string.label_museums);
        } else if (position == 1) {
            return mContext.getString(R.string.label_restaurants);
        } else if (position == 2) {
            return mContext.getString(R.string.label_parks);
        } else {
            return mContext.getString(R.string.label_nature);
        }
    }
}
