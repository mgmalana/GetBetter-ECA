package com.geebeelicious.geebeelicious.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.geebeelicious.geebeelicious.R;
import com.geebeelicious.geebeelicious.adapters.PatientsAdapter;
import com.geebeelicious.geebeelicious.database.DatabaseAdapter;
import com.geebeelicious.geebeelicious.interfaces.ECAActivity;
import com.geebeelicious.geebeelicious.models.consultation.HPI;
import com.geebeelicious.geebeelicious.models.consultation.Patient;
import com.geebeelicious.geebeelicious.models.monitoring.Record;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * The PatientsListActivity serves as the activity allowing
 * the user to view the patient list from a given school.
 * The user can view the list, search for patients, and
 * is given the option to select the patient or be redirected
 * to the module allowing for new patients to be added.
 *
 * @author Katrina Lacsamana
 */

public class PatientListActivity extends ECAActivity{
    /**
     * Used as a flag whether the ECA has spoken.
     */
    private boolean hasSpoken;

    /**
     * Keeps all the patients of the school.
     */
    private ArrayList<Patient> patients = null;

    /**
     * Patient chosen by the user.
     */
    private Patient chosenPatient = null;

    /**
     * Initializes views and other activity objects.
     *
     * @see android.app.Activity#onCreate(Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_list);

        final Typeface chalkFont = Typeface.createFromAsset(getAssets(), "fonts/DJBChalkItUp.ttf");

        DatabaseAdapter getBetterDb = new DatabaseAdapter(PatientListActivity.this);
        try {
            getBetterDb.openDatabaseForRead();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        patients = new ArrayList<>();
        patients = getBetterDb.getPatientsFromSchool(getSchoolPreferences());
        getBetterDb.closeDatabase();

        final EditText inputSearch = (EditText)findViewById(R.id.search_input);
        inputSearch.setTypeface(chalkFont);

        final PatientsAdapter patientsAdapter = new PatientsAdapter(PatientListActivity.this, patients, chalkFont);
        ListView patientListView = (ListView)findViewById(R.id.patientListView);
        patientListView.setAdapter(patientsAdapter);

        integrateECA();

        if(savedInstanceState == null){
            hasSpoken = false;
        } else {
            hasSpoken = savedInstanceState.getBoolean("hasSpoken");
        }

        TextView patientDetailsView = (TextView)findViewById(R.id.patientDetailsTV);
        patientDetailsView.setTypeface(chalkFont);
        patientDetailsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.hideSoftInputFromWindow(inputSearch.getWindowToken(), 0);
            }
        });

        final Button selectPatientButton = (Button)findViewById(R.id.selectPatientButton);
        selectPatientButton.setTypeface(chalkFont);
        selectPatientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PatientListActivity.this, MonitoringConsultationChoice.class);
                intent.putExtra("patient", chosenPatient);
                startActivity(intent);
                finish();
            }
        });

        Button addNewPatientButton = (Button)findViewById(R.id.addPatientButton);
        addNewPatientButton.setTypeface(chalkFont);
        addNewPatientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PatientListActivity.this, AddPatientActivity.class);
                intent.putExtra("schoolID", getSchoolPreferences());
                startActivity(intent);
                finish();
            }
        });

        final Button secretButton = (Button) findViewById(R.id.secretButton);
        secretButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                TextView patientRecordsTextView = (TextView) findViewById(R.id.patientRecordsTextView);
                patientRecordsTextView.setText("");
                patientRecordsTextView.setMovementMethod(new ScrollingMovementMethod());
                patientRecordsTextView.setTypeface(chalkFont);

                DatabaseAdapter getBetterDb = new DatabaseAdapter(PatientListActivity.this);
                try {
                    getBetterDb.openDatabaseForRead();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                ArrayList<Record> records = getBetterDb.getRecords(chosenPatient.getPatientID());
                ArrayList<HPI> hpis = getBetterDb.getHPIs(chosenPatient.getPatientID());

                for (Record record: records){
                    patientRecordsTextView.append(record.getCompleteRecordInfo() + "\n\n");
                }

                for (HPI hpi: hpis){
                    patientRecordsTextView.append(hpi.getCompleteHPIRecord() + "\n\n");
                }

                getBetterDb.closeDatabase();
                return true;
            }
        });


        patientListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                chosenPatient = patients.get(position);
                String patientInfo = "First Name: " + chosenPatient.getFirstName() +
                        "\nLast Name: " + chosenPatient.getLastName() +
                        "\nBirthdate: " + chosenPatient.getBirthday() +
                        "\nGender: " + chosenPatient.getGenderString();
                TextView patientInfoView = (TextView) findViewById(R.id.patientDetailsTV);
                patientInfoView.setTypeface(chalkFont);
                patientInfoView.setText(patientInfo);

                selectPatientButton.setEnabled(true);
                secretButton.setEnabled(true);
                selectPatientButton.setTextColor(Color.WHITE);

                InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.hideSoftInputFromWindow(inputSearch.getWindowToken(), 0);

                ecaFragment.sendToECAToSpeak("Are you " + chosenPatient.getFirstName() +
                        " " + chosenPatient.getLastName() + "?");

            }
        });


        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                patientsAdapter.getFilter().filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                patientsAdapter.filter(s.toString());
            }
        });
    }

    /**
     * Called when the current Window of the activity gains or loses focus.
     * @param hasFocus whether the window of this activity has focus
     *
     * @see android.app.Activity#onWindowFocusChanged(boolean)
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus){
            if(!hasSpoken){
                ecaFragment.sendToECAToSPeak(R.string.patient_list_instruction);
                hasSpoken = true;
            }
        }
    }

    /**
     * Saves {@link #hasSpoken} inside {@code outState}
     *
     * @see android.app.Activity#onSaveInstanceState(Bundle)
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("hasSpoken", hasSpoken);
    }

    /**
     * Called when the activity has detected the user's press of the back key.
     * Starts {@link MainActivity} and ends the current activity.
     */
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(PatientListActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Gets the schoolID of the preferred school stored in device storage.
     * @return ID of the preferred school stored in device storage via Settings.
     */
    private int getSchoolPreferences(){
        int schoolID = 1; //default schoolID
        byte[] byteArray = new byte[4];
        try{
            FileInputStream fis = openFileInput("SchoolIDPreferences");
            fis.read(byteArray, 0, 4);
            fis.close();
        } catch(IOException e){
            return 1;
        }

        ByteBuffer b = ByteBuffer.wrap(byteArray);
        schoolID = b.getInt();

        return schoolID;
    }
}
