package com.keemsa.habittracker;

import android.provider.BaseColumns;

/**
 * Created by sebastian on 11/07/16.
 */
public class HabitTrackerContract {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "HabitTracker.db";

    public HabitTrackerContract() {

    }

    public static abstract class FeedHabit implements BaseColumns {
        public static final String TABLE_NAME = "habit_user";
        public static final String COLUMN_NAME_HABIT_NAME = "habit_name";
        public static final String COLUMN_NAME_HABIT_DATE = "habit_date";
    }
}
