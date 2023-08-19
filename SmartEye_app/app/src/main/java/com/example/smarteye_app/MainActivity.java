package com.example.smarteye_app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    // Permission codes for camera access
    private static final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int CAMERA_REQUEST_CODE = 10;
    private ImageButton howToButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up camera button
        View enableCamera = findViewById(R.id.camImg);
        howToButton = (ImageButton) findViewById(R.id.howto);
        enableCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if((hasAllPermissions())){
                    startCamera();
                }
                else{
                    requestCameraPermission();
                }
            }
        });

        howToButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                howToActivity();
            }
        });

        /*
        howToButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.activity_howto);
                Button goBackButton = (Button) findViewById(R.id.goback);
                goBackButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setContentView(R.layout.activity_main);
                    }
                });
            }
        });

         */

    }

    // True if camera access is granted, false otherwise
    private boolean hasCameraPermission(){
        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasAllPermissions(){
        for(String permission : REQUIRED_PERMISSIONS) {
            if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }

    // Request camera access from user
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                CAMERA_REQUEST_CODE
        );
    }

    // Changes activity to CameraActivity
    private void startCamera(){
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
    }

    private void howToActivity(){
        Intent intent = new Intent(this, howToActivity.class);
        startActivity(intent);
    }
}
