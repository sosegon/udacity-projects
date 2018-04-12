package com.keemsa.todd.data;

import android.content.UriMatcher;
import android.net.Uri;
import android.test.AndroidTestCase;

/**
 * Created by sebastian on 10/09/16.
 */
public class TestUriMatcher extends AndroidTestCase {

    private static final Uri TEST_PATIENT_DIR = ToddContract.PatientEntry.CONTENT_URI;
    private static final Uri TEST_PATIENT_ID = ToddContract.PatientEntry.buildPatientUri("1719873281");
    private static final Uri TEST_PATIENT_TODD = ToddContract.PatientEntry.buildPatientToddUri();

    public void testUriMatcher() {
        UriMatcher testMatcher = ToddProvider.buildUriMatcher();

        assertEquals("Error: The PATIENT URI was matched incorrectly", testMatcher.match(TEST_PATIENT_DIR), ToddProvider.PATIENT);
        assertEquals("Error: The PATIENT ID URI was matched incorrectly", testMatcher.match(TEST_PATIENT_ID), ToddProvider.PATIENT_ID);
        assertEquals("Error: The PATIENT TODD URI was matched incorrectly", testMatcher.match(TEST_PATIENT_TODD), ToddProvider.PATIENT_WITH_TODD);
    }
}
