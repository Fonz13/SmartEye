package com.example.smarteye_app;

import android.content.Intent;
import android.graphics.Camera;
import android.icu.text.DecimalFormat;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.Map;

public class ResultActivity extends AppCompatActivity {
    // Results from questionnaire. Stored here temporary for future usage.
    double irisSize, time = 0;
    private int gender, drunk, drugs, age, stroke, unconscious;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        DecimalFormat numberFormat = new DecimalFormat("0.00");

        // Results from questionnaire. Stored here temporary for future usage.

        int gender, drunk, drugs, age, stroke, unconscious;

        double small = getIntent().getDoubleExtra("EXTRA_SMALL", 0);
        double large = getIntent().getDoubleExtra("EXTRA_LARGE", 0);
        time = getIntent().getDoubleExtra("EXTRA_TIME", 0);
        gender = getIntent().getIntExtra("EXTRA_GENDER",0);
        drunk = getIntent().getIntExtra("EXTRA_DRUNK",0);
        drugs = getIntent().getIntExtra("EXTRA_DRUGS",0);
        age = getIntent().getIntExtra("EXTRA_AGE",0);
        stroke= getIntent().getIntExtra("EXTRA_STROKE",0);
        unconscious = getIntent().getIntExtra("EXTRA_UNCONSCIOUS",0);
        double accuracy = getIntent().getDoubleExtra("EXTRA_ACC", 0);
        double relative = 0;
        if(large > 0 && small > 0){
            relative = (large - small)/ large;
        }

        if(gender == FormActivity.FEMALE){
            irisSize = 11.64;
        } else if(gender == FormActivity.MALE){
            irisSize = 11.77;
        }
        double changeMM = 0;
        if(large > 0 && small > 0){
            changeMM = (large-small)*irisSize;
        }
        small = irisSize*small;

        TextView timeAcc = findViewById(R.id.svar1);
        timeAcc.setText(numberFormat.format(time) + "s");

        TextView percent = findViewById(R.id.svar2);
        percent.setText("Kontraktion: " + numberFormat.format(relative*100) + "%");

        TextView actualSize = findViewById(R.id.svar3);
        actualSize.setText("Kontraktion i mm: " + numberFormat.format(changeMM));

        TextView smal = findViewById(R.id.svar4);
        smal.setText("Träffsäkerhet: " + numberFormat.format(accuracy*100)+"%");



        ImageButton done =  findViewById(R.id.klar);
        done.setOnClickListener(new View.OnClickListener(){
        @Override
        public void onClick(View v){
            onBackPressed();
        }});

        ImageButton camButton =  findViewById(R.id.camera);
        camButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                deleteVideo();
                cameraActivity(v);
            }});

        // For debugging purposes

        FloatingActionButton watch = findViewById(R.id.watch);
        watch.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                previewActivity(v);
            }});

    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        deleteVideo();
        startActivity(intent);
    }

    public void cameraActivity(View view) {
        Intent intent = new Intent(this, CameraActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void previewActivity(View view) {
        Intent intent = new Intent(this, PreviewActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void deleteVideo() {
        File file1 = new File(getExternalMediaDirs()[0].toString() + "/EYE2.mp4");
        File file2 = new File(getExternalMediaDirs()[0].toString() + "/EYE.mp4");
        boolean isSuccess1 = file1.delete();
        boolean isSuccess2 = file2.delete();
    }

}