package com.geebeelicious.geebeelicious.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.geebeelicious.geebeelicious.R;

import java.lang.reflect.Array;
import java.util.ArrayList;

import models.consultation.School;

/**
 * Created by Kate on 03/30/2016.
 */
public class SchoolsAdapter extends ArrayAdapter<School>{

    public SchoolsAdapter(Context context, ArrayList<School> schools){
        super(context, 0, schools);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        School school = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_school_list, parent, false);
        }

        TextView schoolName = (TextView)convertView.findViewById(R.id.school_name_list);
        TextView schoolMunicipality = (TextView) convertView.findViewById(R.id.school_municipality_list);

        schoolName.setText(school.getSchoolName());
        schoolMunicipality.setText(school.getMunicipality());

        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        School school = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_school_list, parent, false);
        }

        TextView schoolName = (TextView)convertView.findViewById(R.id.school_name_list);
        TextView schoolMunicipality = (TextView) convertView.findViewById(R.id.school_municipality_list);

        schoolName.setText(school.getSchoolName());
        schoolMunicipality.setText(school.getMunicipality());

        return convertView;
    }

}