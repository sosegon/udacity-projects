package com.keemsa.todd.data;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

/**
 * Created by sebastian on 10/09/16.
 */
public class TestProvider extends AndroidTestCase {

    public void testProviderRegistry() {
        PackageManager manager = mContext.getPackageManager();

        ComponentName componentName = new ComponentName(mContext.getPackageName(), ToddProvider.class.getName());

        try {
            ProviderInfo providerInfo = manager.getProviderInfo(componentName, 0);

            assertEquals("Error: ToddProvider registered with authority: " + providerInfo.authority + " instead of " + ToddContract.CONTENT_AUTHORITY,
                    providerInfo.authority, ToddContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            assertTrue("Error: ToddProvider not registered at " + mContext.getPackageName(), false);
        }
    }

    public void testGetType() {
        // content://com.keemsa.todd/patient/
        String type = mContext.getContentResolver().getType(ToddContract.PatientEntry.CONTENT_URI);
        assertEquals("Error: the PatientEntry CONTENT_URI should return PatientEntry.CONTENT_TYPE", ToddContract.PatientEntry.CONTENT_TYPE, type);

        // content://com.keemsa.todd/patient/1719873281
        type = mContext.getContentResolver().getType(ToddContract.PatientEntry.buildPatientUri("1719873281"));
        assertEquals("Error: the PatientEntry CONTENT_URI with id return PatientEntry.CONTENT_ITEM_TYPE", ToddContract.PatientEntry.CONTENT_ITEM_TYPE, type);

        // content://com.keemsa.todd/patient/diagnosis/todd
        type = mContext.getContentResolver().getType(ToddContract.PatientEntry.buildPatientToddUri());
        assertEquals("Error: the PatientEntry CONTENT_URI with todd return PatientEntry.CONTENT_TYPE", ToddContract.PatientEntry.CONTENT_TYPE, type);
    }

    public void testBasicPatientQuery() {
        ToddDbHelper dbHelper = new ToddDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testValues = new ContentValues();

        testValues.put(ToddContract.PatientEntry._ID, "1719873281");
        testValues.put(ToddContract.PatientEntry.COLUMN_FIRST_NAME, "Andres");
        testValues.put(ToddContract.PatientEntry.COLUMN_LAST_NAME, "Velasquez");
        testValues.put(ToddContract.PatientEntry.COLUMN_SEX, "male");
        testValues.put(ToddContract.PatientEntry.COLUMN_BIRTH_DATE, "1984-09-23");
        testValues.put(ToddContract.PatientEntry.COLUMN_MIGRAINES, 0);
        testValues.put(ToddContract.PatientEntry.COLUMN_HALLUCINOGENIC_DRUGS, 0);
        testValues.put(ToddContract.PatientEntry.COLUMN_TODD_LIKELIHOOD, 25);

        db.insert(ToddContract.PatientEntry.TABLE_NAME, null, testValues);

        db.close();

        // Test the basic content provider query
        Cursor patientCursor = mContext.getContentResolver().query(
                ToddContract.PatientEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        assertTrue("Empty cursor returned.", patientCursor.moveToFirst());
    }
}
