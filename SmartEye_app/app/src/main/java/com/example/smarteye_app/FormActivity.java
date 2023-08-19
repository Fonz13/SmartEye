package com.example.smarteye_app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.slider.RangeSlider;
import com.google.android.material.slider.Slider;

import java.io.File;

public class FormActivity extends AppCompatActivity {

    ImageButton r;
    private ProcessActivity processActivity;
    public static final int MALE = 0;
    public static final int FEMALE = 1;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);
        r = findViewById(R.id.results);
        processActivity = new ProcessActivity();
        ProgressBar p = (ProgressBar) findViewById(R.id.progressBar);
        p.setVisibility(View.VISIBLE);
        processActivity.setProggressBar(p);
        boolean fail = false;

        String filename = getExternalMediaDirs()[0].toString();

        try {
            processActivity.getFrames(filename, filename);
        } catch (Exception e) {
            Log.e("proccessActivity", "onCreate: ", e);
        }
        while (!processActivity.getDone()) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        p.setVisibility(View.INVISIBLE);
        //r.setText("Resultat");
        //deleteVideo();
        setUpResults();
    }


    public void resultActivity(double size, double time, int drunk, int drugs, int unconscious, int stroke, int age, int gender) {
        Intent intent = new Intent(this, ResultActivity.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("EXTRA_LARGE", processActivity.getLargestPupil());
        Log.i("LARGE", ""+processActivity.getLargestPupil());
        intent.putExtra("EXTRA_SMALL", processActivity.getSmallestPupil());
        intent.putExtra("EXTRA_ACC", processActivity.getHitPercent());
        intent.putExtra("EXTRA_TIME", time);
        intent.putExtra("EXTRA_DRUNK", drunk);
        intent.putExtra("EXTRA_DRUGS", drugs);
        intent.putExtra("EXTRA_UNCONSCIOUS", unconscious);
        intent.putExtra("EXTRA_STROKE", stroke);
        intent.putExtra("EXTRA_AGE", age);
        intent.putExtra("EXTRA_GENDER", gender);


        startActivity(intent);
    }

    public void setUpResults(){
        r.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int gender, drunk, drugs, age, stroke, unconscious;
                RadioGroup alcoholQuestion = (RadioGroup) findViewById(R.id.fraga1ANS);
                RadioGroup drugQuestion = (RadioGroup) findViewById(R.id.fraga2ANS);
                RadioGroup unconsciousQuestion = (RadioGroup) findViewById(R.id.fraga3ANS);
                RadioGroup strokeQuestion = (RadioGroup) findViewById(R.id.fraga4ANS);
                Slider ageQuestion = findViewById(R.id.agebar);
                RadioGroup genderQuestion = (RadioGroup) findViewById(R.id.fraga6ANS);
                //Checks if yes
                if(alcoholQuestion.getCheckedRadioButtonId() == R.id.fraga1YES){
                    drunk = 1;
                } else {drunk = 0;}
                if(drugQuestion.getCheckedRadioButtonId() == R.id.fraga2YES){
                    drugs = 1;
                } else { drugs = 0;}
                if(unconsciousQuestion.getCheckedRadioButtonId() == R.id.fraga3YES){
                    unconscious = 1;
                } else { unconscious = 0;}
                if(strokeQuestion.getCheckedRadioButtonId() == R.id.fraga4YES){
                    stroke = 1;
                } else { stroke = 0;}
                if(genderQuestion.getCheckedRadioButtonId() == R.id.fraga6Male){
                    gender = MALE;
                } else { gender = FEMALE;}
                age = (int) ageQuestion.getValue();

                resultActivity(processActivity.getSizedifference(), processActivity.getTime(), drunk, drugs, unconscious, stroke, age, gender);

            }
        });

    }


    @Override
    public void onBackPressed(){
        Intent intent = new Intent(this, MainActivity.class);
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