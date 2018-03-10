package com.keemsa.habittracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by sebastian on 11/07/16.
 */
public class HabitTrackerDbHelper extends SQLiteOpenHelper {

    private static final String CREATE_HABIT_TABLE = "CREATE TABLE " +
            HabitTrackerContract.FeedHabit.TABLE_NAME + " (" +
            HabitTrackerContract.FeedHabit._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            HabitTrackerContract.FeedHabit.COLUMN_NAME_HABIT_NAME + " TEXT NOT NULL," +
            HabitTrackerContract.FeedHabit.COLUMN_NAME_HABIT_DATE + " TEXT NOT NULL" +
            ")";

    private static final String DELETE_HABIT_TABLE = "DROP TABLE IF EXISTS " + HabitTrackerContract.FeedHabit.TABLE_NAME;

    public HabitTrackerDbHelper(Context context) {
        super(context, HabitTrackerContract.DATABASE_NAME, null, HabitTrackerContract.DATABASE_VERSION);
    }

    public void insertHabitRecord(String habitName, String habitDate) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(HabitTrackerContract.FeedHabit.COLUMN_NAME_HABIT_NAME, habitName);
        values.put(HabitTrackerContract.FeedHabit.COLUMN_NAME_HABIT_DATE, habitDate);

        db.insert(HabitTrackerContract.FeedHabit.TABLE_NAME,
                null,
                values);
    }

    public Cursor queryHabitRecords(String habitName) {
        SQLiteDatabase db = getReadableDatabase();

        String[] columns = {
                HabitTrackerContract.FeedHabit.COLUMN_NAME_HABIT_NAME,
                HabitTrackerContract.FeedHabit.COLUMN_NAME_HABIT_DATE
        };

        String orderBy = HabitTrackerContract.FeedHabit.COLUMN_NAME_HABIT_NAME + " ASC";
        String selection = HabitTrackerContract.FeedHabit.COLUMN_NAME_HABIT_NAME + " LIKE '%" + habitName + "%'";

        return db.query(
                HabitTrackerContract.FeedHabit.TABLE_NAME,
                columns,
                selection,
                null,
                null,
                null,
                orderBy
        );
    }

    public int updateHabitRecords(String habitName1, String habitName2) {
        SQLiteDatabase db = this.getReadableDatabase();

        ContentValues values = new ContentValues();
        values.put(HabitTrackerContract.FeedHabit.COLUMN_NAME_HABIT_NAME, habitName2);

        String selection = HabitTrackerContract.FeedHabit.COLUMN_NAME_HABIT_NAME + " = '" + habitName1 + "'";

        return db.update(
                HabitTrackerContract.FeedHabit.TABLE_NAME,
                values,
                selection,
                null
        );
    }

    public void deleteHabitTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(HabitTrackerContract.FeedHabit.TABLE_NAME, null, null);
    }

    public void deleteDatabase(Context context) {
        context.deleteDatabase(HabitTrackerContract.DATABASE_NAME);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_HABIT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(DELETE_HABIT_TABLE);
        onCreate(sqLiteDatabase);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
