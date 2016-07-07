package com.keemsa.tourguide;

import android.content.Context;
import android.content.res.TypedArray;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sebastian on 06/07/16.
 */
public class ResourceHelper {

    /*
        Implemented from http://basememara.com/storing-multidimensional-resource-arrays-in-android/
     */
    public static List<TypedArray> getMultiTypedArray(Context context, String key) {
        List<TypedArray> values = new ArrayList<>();

        try {
            Class<R.array> resources = R.array.class;
            Field field;
            int c = 0;

            do {
                field = resources.getField(key + "_" + c);
                values.add(context.getResources().obtainTypedArray(field.getInt(null)));
                c++;
            } while (field != null);
        } catch (Exception e) {
            e.printStackTrace();
            ;
        } finally {
            return values;
        }
    }
}
