package com.keemsa.todd.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by sebastian on 10/09/16.
 */
public class ToddDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "todd.db";

    public ToddDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_PATIENT_TABLE = "CREATE TABLE " + ToddContract.PatientEntry.TABLE_NAME + " (" +
                ToddContract.PatientEntry._ID + " TEXT PRIMARY KEY, " +
                ToddContract.PatientEntry.COLUMN_FIRST_NAME + " TEXT NOT NULL, " +
                ToddContract.PatientEntry.COLUMN_LAST_NAME + " TEXT NOT NULL, " +
                ToddContract.PatientEntry.COLUMN_SEX + " TEXT NOT NULL, " +
                ToddContract.PatientEntry.COLUMN_BIRTH_DATE + " TEXT NOT NULL, " +
                ToddContract.PatientEntry.COLUMN_MIGRAINES + " INTEGER NOT NULL, " +
                ToddContract.PatientEntry.COLUMN_HALLUCINOGENIC_DRUGS + " INTEGER NOT NULL, " +
                ToddContract.PatientEntry.COLUMN_TODD_LIKELIHOOD + " INTEGER NOT NULL)";

        sqLiteDatabase.execSQL(SQL_CREATE_PATIENT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // TODO add code depending on what changes are needed in the new version
    }
}
