package com.keemsa.todd;


import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.keemsa.todd.data.Patient;
import com.keemsa.todd.data.ToddContract;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment implements PatientAsyncTask.PatientAsyncResponse {

    private Button btn_add_patient;
    private PatientAdapter adapter;

    public MainFragment() {
        // Required empty public constructor
    }

    public static final int PATIENT_ADD_REQUEST = 1984;
    public static final int PATIENT_ADDED_OK = 1985;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main, container, false);

        // Set listener for button to add new patient
        btn_add_patient = (Button) view.findViewById(R.id.btn_add_patient);
        btn_add_patient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), NewPatientActivity.class);
                startActivityForResult(intent, PATIENT_ADD_REQUEST);
            }
        });

        adapter = new PatientAdapter(getContext(), 0, new ArrayList<Patient>());
        ListView listView = (ListView) view.findViewById(R.id.lv_patients);
        listView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onStart() {

        PatientAsyncTask task = new PatientAsyncTask(this);
        task.execute(getContext());

        super.onStart();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == PATIENT_ADD_REQUEST && resultCode == PATIENT_ADDED_OK){
            Toast.makeText(getContext(), "New patient added", Toast.LENGTH_SHORT).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void processCursor(Cursor cursor) {
        List<Patient> patients = new ArrayList<Patient>();
        if(cursor.moveToFirst()){
            while(!cursor.isAfterLast()){
                String id = cursor.getString(cursor.getColumnIndex(ToddContract.PatientEntry._ID));
                String firstName = cursor.getString(cursor.getColumnIndex(ToddContract.PatientEntry.COLUMN_FIRST_NAME));
                String lastName = cursor.getString(cursor.getColumnIndex(ToddContract.PatientEntry.COLUMN_LAST_NAME));
                String sex = cursor.getString(cursor.getColumnIndex(ToddContract.PatientEntry.COLUMN_SEX));
                String birthDate = cursor.getString(cursor.getColumnIndex(ToddContract.PatientEntry.COLUMN_BIRTH_DATE));
                int migraines = cursor.getInt(cursor.getColumnIndex(ToddContract.PatientEntry.COLUMN_MIGRAINES));
                int hallucinogenicDrugs = cursor.getInt(cursor.getColumnIndex(ToddContract.PatientEntry.COLUMN_HALLUCINOGENIC_DRUGS));
                int toddLikelihood = cursor.getInt(cursor.getColumnIndex(ToddContract.PatientEntry.COLUMN_TODD_LIKELIHOOD));

                Patient patient = new Patient(id, firstName, lastName, sex, birthDate, migraines, hallucinogenicDrugs, toddLikelihood);
                patients.add(patient);

                cursor.moveToNext();
            }
        }

        if(adapter != null){
            adapter.clear();

            for(Patient patient : patients){
                adapter.add(patient);
            }
        }
    }
}
