package com.keemsa.todd;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.keemsa.todd.data.Patient;

import java.util.List;

/**
 * Created by sebastian on 10/09/16.
 */
public class PatientAdapter extends ArrayAdapter<Patient> {

    public PatientAdapter(Context context, int resource, List<Patient> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Patient patient = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_patient, parent, false);
        }

        TextView ipt_first_name = (TextView) convertView.findViewById(R.id.ipt_first_name);
        TextView ipt_last_name = (TextView) convertView.findViewById(R.id.ipt_last_name);
        TextView ipt_todd_likelihood = (TextView) convertView.findViewById(R.id.ipt_todd_likelihood);

        ipt_first_name.setText(patient.getFirstName());
        ipt_last_name.setText(patient.getLastName());
        ipt_todd_likelihood.setText(String.valueOf(patient.getToddLikelihood()));

        return convertView;
    }
}
