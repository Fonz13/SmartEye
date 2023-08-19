package com.example.smarteye_app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;


//OVERRIDE BACKBUTTON
public class PreviewActivity extends AppCompatActivity {
    FloatingActionButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        //visa preview
        VideoView mVideoView = (VideoView) findViewById(R.id.video_view);
        mVideoView.setVideoURI(Uri.parse(getExternalMediaDirs()[0].toString() + "/EYE2.mp4"));
        mVideoView.setMediaController(new MediaController(this));
        mVideoView.requestFocus();
        mVideoView.start();

        //tillbaka till kamera
        backButton = findViewById(R.id.backToResults);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resultActivity(v);

            }
        });
    }

    public void CameraActivity(View view) {
        Intent intent = new Intent(this, CameraActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        deleteVideo();
        startActivity(intent);
    }

    public void resultActivity(View view) {
        Intent intent = new Intent(this, ResultActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        //Dont delete video here, if the user wants to watch again.
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        deleteVideo();
        startActivity(intent);
    }

    public void deleteVideo() {
        File file1 = new File(getExternalMediaDirs()[0].toString() + "/EYE2.mp4");
        File file2 = new File(getExternalMediaDirs()[0].toString() + "/EYE.mp4");
        boolean isSuccess1 = file1.delete();
        boolean isSuccess2 = file2.delete();
    }
}
