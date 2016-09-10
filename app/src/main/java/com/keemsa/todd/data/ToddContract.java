package com.keemsa.todd.data;

import android.provider.BaseColumns;

/**
 * Created by sebastian on 10/09/16.
 */
public class ToddContract {

    public static final class PatientEntry implements BaseColumns {

        public static final String TABLE_NAME = "patient";

        public static final String COLUMN_FIRST_NAME = "first_name";
        public static final String COLUMN_LAST_NAME = "last_name";
        public static final String COLUMN_SEX = "sex";
        public static final String COLUMN_BIRTH_DATE = "birth_date";
        public static final String COLUMN_MIGRAINES = "migraines";
        public static final String COLUMN_HALLUCINOGENIC_DRUGS = "hallucinogenic_drugs";
        public static final String COLUMN_TODD_LIKELIHOOD = "todd_likelihood";
    }
}
