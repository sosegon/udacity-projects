package com.keemsa.habittracker;

import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    HabitTrackerDbHelper dbHelper;
    String[] habits = {
            "wake up",
            "eat breakfast",
            "walk to work",
            "eat lunch",
            "do laundry",
            "read book",
            "watch netflix",
            "jogging"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new HabitTrackerDbHelper(MainActivity.this);

        Calendar cal = Calendar.getInstance();
        for (int i = 0; i < habits.length; i++) {
            dbHelper.insertHabitRecord(habits[i], cal.getTime().toString());
            dbHelper.insertHabitRecord(habits[i], cal.getTime().toString());
            Log.i("DATABASE", "new record added to table " + HabitTrackerContract.FeedHabit.TABLE_NAME);
        }

        Cursor c = dbHelper.queryHabitRecords("read");
        Log.i("DATABASE", c.getCount() + " records with habit 'read' found");

        int u = dbHelper.updateHabitRecords("wake up", "get up");
        Log.i("DATABASE", u + " records updated from 'wake up' to 'get up'");

        dbHelper.deleteHabitTable();
        Log.i("DATABASE", "table " + HabitTrackerContract.FeedHabit.TABLE_NAME + " deleted");

        dbHelper.deleteDatabase(MainActivity.this);
        Log.i("DATABASE", "database " + HabitTrackerContract.DATABASE_NAME + " deleted");
    }
}
