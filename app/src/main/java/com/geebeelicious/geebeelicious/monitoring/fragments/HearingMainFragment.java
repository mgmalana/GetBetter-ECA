package com.geebeelicious.geebeelicious.monitoring.fragments;


import android.app.Activity;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.geebeelicious.geebeelicious.R;
import com.geebeelicious.geebeelicious.monitoring.MonitoringFragmentInteraction;

import java.util.ArrayList;

import models.hearing.HearingTest;
import models.monitoring.Record;

/**
 * Created by Kate.
 * The HearingMainFragment serves as the fragment
 * for the hearing test. It uses the HearingTest class
 * to perform the hearing test.
 */

public class HearingMainFragment extends Fragment {
    private Record record;
    private MonitoringFragmentInteraction fragmentInteraction;

    private ArrayList<Thread> threads;
    private HearingTest hearingTest;
    private Activity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view =  inflater.inflate(R.layout.fragment_hearing_main, container, false);
        AudioManager audioManager = (AudioManager)activity.getSystemService(activity.AUDIO_SERVICE);

        hearingTest = new HearingTest();
        final double[] calibrationData = hearingTest.getCalibrationData(activity);

        final Button yesButton = (Button)view.findViewById(R.id.YesButton);
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hearingTest.setHeard();
            }
        });

        final Runnable backgroundFlash = new Runnable(){
            public void run(){
                yesButton.setBackgroundColor(Color.parseColor("#18FFFF"));
            }
        };

        final Runnable backgroundNormal = new Runnable(){
            public void run(){
                yesButton.setBackgroundColor(Color.parseColor("#80DEEA"));
            }
        };

        final Runnable disableTest = new Runnable() {
            @Override
            public void run() {
                yesButton.setVisibility(View.GONE);
                yesButton.setEnabled(false);
                ImageView imageView = (ImageView)view.findViewById(R.id.hearingTestImageView);
                imageView.setImageResource(R.drawable.wait_for_next_test);
                TextView resultsView = (TextView) view.findViewById(R.id.hearingResultsTV);
                resultsView.setText(hearingTest.getResults());
            }
        };

        Thread screenThread = new Thread(new Runnable(){
            public void run(){
                while(hearingTest.isInLoop()){
                    if(!hearingTest.isRunning()){
                        return;
                    }
                    if(hearingTest.isHeard()){
                        activity.runOnUiThread(backgroundFlash);
                        while(hearingTest.isHeard()){

                        }
                    }
                }
            }
        });

        Thread timingThread = new Thread(new Runnable(){
            public void run(){
                while(hearingTest.isInLoop()){
                    if(!hearingTest.isRunning()){
                        return;
                    }
                    if(hearingTest.isHeard()){
                        try{
                            Thread.sleep(500);
                        } catch(InterruptedException ie){

                        }
                        activity.runOnUiThread(backgroundNormal);
                    }
                }
            }
        });

        Thread testThread = new Thread(new Runnable() {
            @Override
            public void run() {
                hearingTest.performTest(calibrationData);
                if(hearingTest.isDone()){
                    activity.runOnUiThread(backgroundFlash);
                    activity.runOnUiThread(disableTest);
                    endTest();
                    try {
                        Thread.sleep(6000);
                    } catch (InterruptedException e) {

                    }
                    fragmentInteraction.doneFragment();
                }
            }
        });

        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 9, 0);
        threads = new ArrayList<Thread>();
        threads.add(screenThread);
        threads.add(timingThread);
        threads.add(testThread);
        screenThread.start();
        timingThread.start();
        testThread.start();

        return view;
    }

    private void endTest(){
        record.setHearingRight(hearingTest.getPureToneAverageInterpretation("Right"));
        record.setHearingLeft(hearingTest.getPureToneAverageInterpretation("Left"));

        stopTest();
    }

    private void stopTest(){
        hearingTest.setIsNotRunning();
        for(int i = 0; i<threads.size(); i++){
            threads.get(i).interrupt();
        }
    }

    //For testing purposes only
    public void endTestShortCut(){
        record.setHearingRight("Mild Hearing Loss");
        record.setHearingLeft("Moderately-Severe Hearing Loss");

        stopTest();
        fragmentInteraction.doneFragment();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            fragmentInteraction = (MonitoringFragmentInteraction) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement MonitoringFragmentInteraction");
        }
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        record = fragmentInteraction.getRecord();
    }

}