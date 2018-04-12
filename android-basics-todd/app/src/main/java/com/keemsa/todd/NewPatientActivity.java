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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class NewPatientActivity extends AppCompatActivity {

    private EditText txt_id, txt_first_name, txt_last_name;
    private Button txt_sex, txt_birth_date, txt_migraines, txt_hallucinogenic_drugs;

    private Button btn_insert_patient;
    private Patient patient = new Patient();

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

        patient.setId("");
        patient.setFirstName("");
        patient.setLastName("");
        patient.setSex(getString(R.string.lbl_male));
        patient.setBirthDate(getCurrentDate());
        patient.setMigraines(0);
        patient.setHallucinogenicDrugs(0);

        txt_sex.setText(getString(R.string.lbl_sex) + ": " + patient.getSex());
        txt_migraines.setText(getString(R.string.lbl_migraines) + ": " + patient.getMigrainesAsString());
        txt_hallucinogenic_drugs.setText(getString(R.string.lbl_hallucinogenic_drugs) + ": " + patient.getHallucinogenicDrugsAsString());
        txt_birth_date.setText(getString(R.string.lbl_birth_date) + ": " + patient.getBirthDate());

        btn_insert_patient = (Button) findViewById(R.id.btn_insert_patient);
        btn_insert_patient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String id = txt_id.getText().toString();
                String firstName = txt_first_name.getText().toString();
                String lastName = txt_last_name.getText().toString();

                patient.setId(id);
                patient.setFirstName(firstName);
                patient.setLastName(lastName);

                if (!checkValidPatient(patient)) {
                    Toast.makeText(NewPatientActivity.this, getString(R.string.msg_fill_add_fields), Toast.LENGTH_LONG).show();
                    return;
                }

                if (patientExist(patient)) {
                    Toast.makeText(NewPatientActivity.this, getString(R.string.msg_patient_exist) +
                                    " " + patient.getId() + " " + getString(R.string.msg_patient_exist_tail),
                            Toast.LENGTH_LONG).show();
                    return;
                }

                patient.calcToddLikelihood();

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

    private void displaySexDialog() {
        AlertDialog.Builder sexDialog = new AlertDialog.Builder(NewPatientActivity.this);
        sexDialog.setTitle(getString(R.string.lbl_sex));
        sexDialog.setItems(R.array.opt_sex, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0) {
                    patient.setSex(getString(R.string.lbl_male));
                } else if (i == 1) {
                    patient.setSex(getString(R.string.lbl_female));
                }
                txt_sex.setText(getString(R.string.lbl_sex) + ": " + patient.getSex());
            }
        });
        sexDialog.show();
    }

    private void displayMigrainesDialog() {
        AlertDialog.Builder migrainesDialog = new AlertDialog.Builder(NewPatientActivity.this);
        migrainesDialog.setTitle(getString(R.string.lbl_migraines));
        migrainesDialog.setItems(R.array.opt_boolean, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                patient.setMigraines(i);
                txt_migraines.setText(getString(R.string.lbl_migraines) + ": " + patient.getMigrainesAsString());
            }
        });
        migrainesDialog.show();
    }

    private void displayHdrugsDialog() {
        AlertDialog.Builder hdrugsDialog = new AlertDialog.Builder(NewPatientActivity.this);
        hdrugsDialog.setTitle(getString(R.string.lbl_hallucinogenic_drugs));
        hdrugsDialog.setItems(R.array.opt_boolean, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                patient.setHallucinogenicDrugs(i);
                txt_hallucinogenic_drugs.setText(getString(R.string.lbl_hallucinogenic_drugs) + ": " + patient.getHallucinogenicDrugsAsString());
            }
        });
        hdrugsDialog.show();
    }

    private DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
            patient.setBirthDate(new StringBuilder().append(i).append("-").append(i1 + 1).append("-").append(i2).toString());
            txt_birth_date.setText(getString(R.string.lbl_birth_date) + ": " + patient.getBirthDate());
        }
    };

    private boolean checkValidPatient(Patient patient) {
        if (patient.getId().equals(""))
            return false;
        if (patient.getFirstName().equals(""))
            return false;
        if (patient.getLastName().equals(""))
            return false;
        if (!(patient.getSex().equals(getString(R.string.lbl_male)) || patient.getSex().equals(getString(R.string.lbl_female))))
            return false;
        if (!patient.getBirthDate().matches("\\d{4}-\\d{1,2}-\\d{1,2}"))
            return false;
        if (!(patient.getHallucinogenicDrugs() == 0 || patient.getHallucinogenicDrugs() == 1))
            return false;
        if (!(patient.getMigraines() == 0 || patient.getMigraines() == 1))
            return false;

        return true;
    }

    private boolean patientExist(Patient patient) {
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(ToddContract.PatientEntry.CONTENT_URI.buildUpon().appendPath(patient.getId()).build(), null, null, null, null, null);
        if (cursor.moveToFirst()) {
            return true;
        }
        return false;
    }

    private String getCurrentDate() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar current = Calendar.getInstance();

        return dateFormat.format(current.getTime());
    }
}
