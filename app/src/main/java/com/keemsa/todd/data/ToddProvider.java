package com.keemsa.todd.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.CancellationSignal;
import android.support.annotation.Nullable;

/**
 * Created by sebastian on 10/09/16.
 */
public class ToddProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private ToddDbHelper mOpenHelper;

    static final int PATIENT = 100;
    static final int PATIENT_ID = 200;
    static final int PATIENT_WITH_TODD = 300;

    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = ToddContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, ToddContract.PATH_PATIENT, PATIENT);
        matcher.addURI(authority, ToddContract.PATH_PATIENT + "/*", PATIENT_ID);
        matcher.addURI(authority, ToddContract.PATH_PATIENT + "/*/*", PATIENT_WITH_TODD);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new ToddDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {

        final int match = sUriMatcher.match(uri);

        switch (match) {
            case PATIENT:
                return ToddContract.PatientEntry.CONTENT_TYPE;
            case PATIENT_ID:
                return ToddContract.PatientEntry.CONTENT_ITEM_TYPE;
            case PATIENT_WITH_TODD:
                return ToddContract.PatientEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder, CancellationSignal cancellationSignal) {

        Cursor cursor;

        switch (sUriMatcher.match(uri)) {
            case PATIENT:
                cursor = mOpenHelper.getReadableDatabase().query(
                        ToddContract.PatientEntry.TABLE_NAME,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );
                break;
            case PATIENT_ID:
                String patientId = ToddContract.PatientEntry.getPatientIdFromUri(uri);
                cursor = mOpenHelper.getReadableDatabase().query(
                        ToddContract.PatientEntry.TABLE_NAME,
                        null,
                        ToddContract.PatientEntry._ID + "=" + patientId,
                        null,
                        null,
                        null,
                        null
                );
                break;
            case PATIENT_WITH_TODD:
                cursor = mOpenHelper.getReadableDatabase().query(
                        ToddContract.PatientEntry.TABLE_NAME,
                        null,
                        ToddContract.PatientEntry.COLUMN_TODD_LIKELIHOOD + "=" + 100,
                        null,
                        null,
                        null,
                        null
                );
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] strings, String s, String[] strings1, String s1) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case PATIENT:
                long id = db.insert(ToddContract.PatientEntry.TABLE_NAME, null, contentValues);
                if (id > 0) {
                    returnUri = ToddContract.PatientEntry.buildPatientUri(contentValues.getAsString(ToddContract.PatientEntry._ID));
                } else {
                    throw new SQLiteException("Failed to insert row into " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return returnUri;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        int rowsDeleted;

        switch (match) {
            case PATIENT:
                rowsDeleted = db.delete(ToddContract.PatientEntry.TABLE_NAME, s, strings);
                break;
            case PATIENT_ID:
                rowsDeleted = db.delete(ToddContract.PatientEntry.TABLE_NAME, s, strings);
                break;
            case PATIENT_WITH_TODD:
                rowsDeleted = db.delete(ToddContract.PatientEntry.TABLE_NAME, s, strings);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        int rowsUpdated;

        switch (match) {
            case PATIENT:
                rowsUpdated = db.update(ToddContract.PatientEntry.TABLE_NAME, contentValues, s, strings);
                break;
            case PATIENT_ID:
                rowsUpdated = db.update(ToddContract.PatientEntry.TABLE_NAME, contentValues, s, strings);
                break;
            case PATIENT_WITH_TODD:
                rowsUpdated = db.update(ToddContract.PatientEntry.TABLE_NAME, contentValues, s, strings);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }
}
