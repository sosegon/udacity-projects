package com.keemsa.todd;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;

import com.keemsa.todd.data.ToddContract;

/**
 * Created by sebastian on 10/09/16.
 */
public class PatientAsyncTask extends AsyncTask<Context, Void, Cursor> {

    public interface PatientAsyncResponse{
        void processCursor(Cursor cursor);
    }

    private PatientAsyncResponse response;

    public PatientAsyncTask(PatientAsyncResponse response) {
        this.response = response;
    }

    @Override
    protected Cursor doInBackground(Context... contexts) {
        ContentResolver resolver = contexts[0].getContentResolver();

        return resolver.query(ToddContract.PatientEntry.CONTENT_URI, null, null, null, null, null);
    }

    @Override
    protected void onPostExecute(Cursor cursor) {
        response.processCursor(cursor);
    }
}
