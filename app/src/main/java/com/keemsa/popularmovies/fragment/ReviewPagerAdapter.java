package com.keemsa.popularmovies.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

/**
 * Created by sebastian on 10/12/16.
 * Based on the post http://tumble.mlcastle.net/post/25875136857/bridging-cursorloaders-and-viewpagers-on-android
 */
public class ReviewPagerAdapter<F extends Fragment> extends FragmentStatePagerAdapter {

    private Class<F> fragmentClass;
    private String[] projection;
    private Cursor cursor;

    private String LOG_TAG = ReviewPagerAdapter.class.getSimpleName();

    public ReviewPagerAdapter(FragmentManager fm, Class<F> fragmentClass, String[] projection, Cursor cursor){
        super(fm);
        this.fragmentClass = fragmentClass;
        this.projection = projection;
        this.cursor = cursor;
    }

    /*
     * This is done to avoid exceptions since the fragments are removed
     * from MovieReviewsFragment
     * http://stackoverflow.com/a/28091395/1065981
     */
    @Override
    public Parcelable saveState() {
        return null;
    }

    @Override
    public F getItem(int position) {
        if(cursor == null){
            return null;
        }

        cursor.moveToPosition(position);
        F fragment;
        try{
            fragment = fragmentClass.newInstance();
        }
        catch (Exception e){
            Log.e(LOG_TAG, "Error creating new fragment");
            throw new RuntimeException(e);
        }

        Bundle args = new Bundle();
        for(int i = 0; i < projection.length; i ++){
            args.putString(projection[i], cursor.getString(i));
        }

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getCount() {
        if(cursor == null){
            return 0;
        }
        else{
            return cursor.getCount();
        }
    }

    public void swapCursor(Cursor newCursor){
        if(newCursor == cursor){
            return;
        }

        this.cursor = newCursor;
        notifyDataSetChanged();
    }

    public Cursor getCursor(){
        return cursor;
    }


}
