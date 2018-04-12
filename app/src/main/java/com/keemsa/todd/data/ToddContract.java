package com.keemsa.todd.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by sebastian on 10/09/16.
 */
public class ToddContract {

    public static final String CONTENT_AUTHORITY = "com.keemsa.todd";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_PATIENT = "patient";

    public static final class PatientEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_PATIENT).build();
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PATIENT;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PATIENT;

        public static final String TABLE_NAME = "patient";

        public static final String COLUMN_FIRST_NAME = "first_name";
        public static final String COLUMN_LAST_NAME = "last_name";
        public static final String COLUMN_SEX = "sex";
        public static final String COLUMN_BIRTH_DATE = "birth_date";
        public static final String COLUMN_MIGRAINES = "migraines";
        public static final String COLUMN_HALLUCINOGENIC_DRUGS = "hallucinogenic_drugs";
        public static final String COLUMN_TODD_LIKELIHOOD = "todd_likelihood";

        public static Uri buildPatientUri(String id) {
            return CONTENT_URI.buildUpon().appendPath(id).build();
        }

        public static Uri buildPatientToddUri() {
            return CONTENT_URI.buildUpon().appendPath("DIAGNOSIS").appendPath("TODD").build();
        }

        public static String getPatientIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }
}
