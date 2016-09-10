package com.keemsa.todd.data;

import android.net.Uri;
import android.test.AndroidTestCase;

/**
 * Created by sebastian on 10/09/16.
 */
public class TestToddContract extends AndroidTestCase {

    private static final String TEST_PATIENT_ID = "/1719873281";

    public void testBuildPatientUri() {
        Uri patientUri = ToddContract.PatientEntry.buildPatientUri(TEST_PATIENT_ID);

        assertNotNull("Error: Null uri returner", patientUri);
        assertEquals("Error: Patient id not added properly to the end of the uri", TEST_PATIENT_ID, patientUri.getLastPathSegment());
        assertEquals("Error: Patient uri does not match expected result", patientUri.toString(), "content://com.keemsa.todd/patient/%2F1719873281");
    }
}
