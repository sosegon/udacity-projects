package com.keemsa.todd.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.HashSet;

/**
 * Created by sebastian on 10/09/16.
 */
public class TestDb extends AndroidTestCase {

    // to start each test with a clean slate
    void deleteTheDatabase() {
        mContext.deleteDatabase(ToddDbHelper.DATABASE_NAME);
    }

    public void setUp() {
        deleteTheDatabase();
    }

    public void testCreateDb() throws Throwable {

        // This HashSet is to verify the tables in the database
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(ToddContract.PatientEntry.TABLE_NAME);

        deleteTheDatabase();
        SQLiteDatabase db = new ToddDbHelper(mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        assertTrue("Error: The database has not been created correctly", cursor.moveToFirst());

        // Check if the database contains the right table
        do {
            tableNameHashSet.remove(cursor.getString(0));
        }
        while (cursor.moveToNext());

        assertTrue("Error: The database was created without the patient entry table", tableNameHashSet.isEmpty());

        // check if the table contains the right columns
        cursor = db.rawQuery("PRAGMA table_info(" + ToddContract.PatientEntry.TABLE_NAME + ")", null);
        assertTrue("Error: Not possible to query the database for table information", cursor.moveToFirst());

        // This HashSet is to verify the columns in the patient entry table
        final HashSet<String> patientColumnHashSet = new HashSet<String>();
        patientColumnHashSet.add(ToddContract.PatientEntry._ID);
        patientColumnHashSet.add(ToddContract.PatientEntry.COLUMN_FIRST_NAME);
        patientColumnHashSet.add(ToddContract.PatientEntry.COLUMN_LAST_NAME);
        patientColumnHashSet.add(ToddContract.PatientEntry.COLUMN_SEX);
        patientColumnHashSet.add(ToddContract.PatientEntry.COLUMN_BIRTH_DATE);
        patientColumnHashSet.add(ToddContract.PatientEntry.COLUMN_MIGRAINES);
        patientColumnHashSet.add(ToddContract.PatientEntry.COLUMN_HALLUCINOGENIC_DRUGS);
        patientColumnHashSet.add(ToddContract.PatientEntry.COLUMN_TODD_LIKELIHOOD);

        int columnNameIndex = cursor.getColumnIndex("name");
        do {
            String columnName = cursor.getString(columnNameIndex);
            patientColumnHashSet.remove(columnName);
        }
        while (cursor.moveToNext());

        assertTrue("Error: The database does not contain all the required patient entry columns", patientColumnHashSet.isEmpty());

        db.close();
    }

    public void testPatientTable() {
        SQLiteDatabase db = new ToddDbHelper(mContext).getWritableDatabase();

        ContentValues testValues = new ContentValues();

        testValues.put(ToddContract.PatientEntry._ID, "1719873281");
        testValues.put(ToddContract.PatientEntry.COLUMN_FIRST_NAME, "Andres");
        testValues.put(ToddContract.PatientEntry.COLUMN_LAST_NAME, "Velasquez");
        testValues.put(ToddContract.PatientEntry.COLUMN_SEX, "male");
        testValues.put(ToddContract.PatientEntry.COLUMN_BIRTH_DATE, "1984-09-23");
        testValues.put(ToddContract.PatientEntry.COLUMN_MIGRAINES, 0);
        testValues.put(ToddContract.PatientEntry.COLUMN_HALLUCINOGENIC_DRUGS, 0);
        testValues.put(ToddContract.PatientEntry.COLUMN_TODD_LIKELIHOOD, 25);

        // Insert testValues in the database and get row id
        long id1 = db.insert(ToddContract.PatientEntry.TABLE_NAME, null, testValues);
        long id2 = db.insert(ToddContract.PatientEntry.TABLE_NAME, null, testValues);

        // Check if the record was inserted correctly
        assertTrue(id1 != -1);

        // Check if the same record was not inserted again
        assertTrue(id2 == -1);

        // Query the database for the recent record
        Cursor cursor = db.query(ToddContract.PatientEntry.TABLE_NAME,
                null,
                ToddContract.PatientEntry._ID + "=" + "1719873281",
                null,
                null,
                null,
                null
        );

        assertTrue("Error: Empty cursor returned", cursor.moveToFirst());
        assertFalse("Error: More than one record returned from patient query", cursor.moveToNext());

        // Close resources
        cursor.close();
        db.close();
    }
}
