package com.keemsa.todd;

import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.keemsa.todd.data.Patient;
import com.keemsa.todd.data.ToddContract;

import java.util.Calendar;

public class NewPatientActivity extends AppCompatActivity {

    private EditText txt_id, txt_first_name, txt_last_name;
    private Button txt_sex, txt_birth_date, txt_migraines, txt_hallucinogenic_drugs;

    private Button btn_insert_patient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_patient);

        txt_id = (EditText) findViewById(R.id.txt_id);
        txt_first_name = (EditText) findViewById(R.id.txt_first_name);
        txt_last_name = (EditText) findViewById(R.id.txt_last_name);
        txt_sex = (Button) findViewById(R.id.txt_sex);
        txt_birth_date = (Button) findViewById(R.id.txt_birth_date);
        txt_migraines = (Button) findViewById(R.id.txt_migraines);
        txt_hallucinogenic_drugs = (Button) findViewById(R.id.txt_hallucinogenic_drugs);

        btn_insert_patient = (Button) findViewById(R.id.btn_insert_patient);
        btn_insert_patient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Patient patient = new Patient(
                        txt_id.getText().toString(),
                        txt_first_name.getText().toString(),
                        txt_last_name.getText().toString(),
                        txt_sex.getText().toString(),
                        txt_birth_date.getText().toString(),
                        txt_migraines.getText().toString(),
                        txt_hallucinogenic_drugs.getText().toString());

                if(!checkPatient(patient)){
                    Toast.makeText(NewPatientActivity.this, "Fill all fields", Toast.LENGTH_LONG).show();
                    return;
                }

                if(patientExist(patient)){
                    Toast.makeText(NewPatientActivity.this, "Patient with ID: " + patient.getId() + " already exists", Toast.LENGTH_LONG).show();
                    return;
                }

                ContentResolver resolver = getContentResolver();

                ContentValues patientValues = new ContentValues();
                patientValues.put(ToddContract.PatientEntry._ID, patient.getId());
                patientValues.put(ToddContract.PatientEntry.COLUMN_FIRST_NAME, patient.getFirstName());
                patientValues.put(ToddContract.PatientEntry.COLUMN_LAST_NAME, patient.getLastName());
                patientValues.put(ToddContract.PatientEntry.COLUMN_SEX, patient.getSex());
                patientValues.put(ToddContract.PatientEntry.COLUMN_BIRTH_DATE, patient.getBirthDate());
                patientValues.put(ToddContract.PatientEntry.COLUMN_MIGRAINES, patient.getMigraines());
                patientValues.put(ToddContract.PatientEntry.COLUMN_HALLUCINOGENIC_DRUGS, patient.getHallucinogenicDrugs());
                patientValues.put(ToddContract.PatientEntry.COLUMN_TODD_LIKELIHOOD, patient.getToddLikelihood());

                resolver.insert(ToddContract.PatientEntry.CONTENT_URI, patientValues);

                Intent output = new Intent();
                setResult(MainFragment.PATIENT_ADDED_OK, output);
                finish();
            }
        });

        txt_sex.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displaySexDialog();
            }
        });
        txt_birth_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Use the current date as the default date in the picker
                final Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);

                // Create a new instance of DatePickerDialog and return it
                DatePickerDialog picker = new DatePickerDialog(NewPatientActivity.this, dateSetListener, year, month, day);
                picker.show();
            }
        });
        txt_migraines.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayMigrainesDialog();
            }
        });
        txt_hallucinogenic_drugs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayHdrugsDialog();
            }
        });
    }

    private void displaySexDialog(){
        AlertDialog.Builder sexDialog = new AlertDialog.Builder(NewPatientActivity.this);
        sexDialog.setTitle("Sex");
        sexDialog.setItems(R.array.opt_sex, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(i == 0){
                    txt_sex.setText("male");
                }
                else if (i == 1){
                    txt_sex.setText("female");
                }
            }
        });
        sexDialog.show();
    }

    private void displayMigrainesDialog(){
        AlertDialog.Builder migrainesDialog = new AlertDialog.Builder(NewPatientActivity.this);
        migrainesDialog.setTitle("Migraines");
        migrainesDialog.setItems(R.array.opt_boolean, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(i == 0){
                    txt_migraines.setText("yes");
                }
                else if (i == 1){
                    txt_migraines.setText("no");
                }
            }
        });
        migrainesDialog.show();
    }

    private void displayHdrugsDialog(){
        AlertDialog.Builder hdrugsDialog = new AlertDialog.Builder(NewPatientActivity.this);
        hdrugsDialog.setTitle("Hallucinogenic drugs");
        hdrugsDialog.setItems(R.array.opt_boolean, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(i == 0){
                    txt_hallucinogenic_drugs.setText("yes");
                }
                else if (i == 1){
                    txt_hallucinogenic_drugs.setText("no");
                }
            }
        });
        hdrugsDialog.show();
    }

    private DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener(){
        @Override
        public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
            txt_birth_date.setText(new StringBuilder().append(i).append("-").append(i1).append("-").append(i2));
        }
    };

    private boolean checkPatient(Patient patient){
        if(patient.getId().equals(""))
            return false;
        if(patient.getFirstName().equals(""))
            return false;
        if(patient.getLastName().equals(""))
            return false;
        if(patient.getSex().equals("Sex"))
            return false;
        if(patient.getBirthDate().equals("Birth date"))
            return  false;

        return true;
    }

    private boolean patientExist(Patient patient){
        ContentResolver resolver = getContentResolver();
        Cursor cursor= resolver.query(ToddContract.PatientEntry.CONTENT_URI.buildUpon().appendPath(patient.getId()).build(), null, null, null, null, null);
        if(cursor.moveToFirst()){
            return true;
        }
        return false;
    }
}
