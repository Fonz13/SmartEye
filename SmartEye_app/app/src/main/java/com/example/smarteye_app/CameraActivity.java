package com.example.smarteye_app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.strictmode.FileUriExposedViolation;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.MeteringPoint;
import androidx.camera.core.MeteringPointFactory;
import androidx.camera.core.Preview;
import androidx.camera.core.VideoCapture;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.awt.font.NumericShaper;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class CameraActivity extends AppCompatActivity {
    private final int FLASHLIGHT_DELAY = 500; // Delay before turning on flashlight (millis)
    private final int RECORDING_LENGTH = 3000; // Recording length (millis)


    private PreviewView previewView;
    private ImageButton captureBtn;
    private ImageView red_circle;
    private ImageView green_circle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // Set up UI elements
        previewView = findViewById(R.id.previewView);
        captureBtn = findViewById(R.id.captureButton);
        red_circle = findViewById(R.id.red_circle);
        green_circle = findViewById(R.id.green_circle);

        // Set up camera access
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    SetUpCamera(cameraProvider);

                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, ContextCompat.getMainExecutor(this));

    }

    public void MainActivity(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
    /*
    public void QuestionActivity(View view) {
        Intent intent = new Intent(this, QuestionActivity.class);
        startActivity(intent);
    }*/

    public void formActivity(View view) {
        Intent intent = new Intent(this, FormActivity.class);
        startActivity(intent);
    }

    private void tapToFocus(Camera camera){
       previewView.setOnTouchListener(new View.OnTouchListener() {
           @SuppressLint("ClickableViewAccessibility")
           @Override
           public boolean onTouch(View v, MotionEvent event) {
               if(event.getAction() == MotionEvent.ACTION_DOWN){
                   return true;
               }
               if(event.getAction() == MotionEvent.ACTION_UP) {
                   MeteringPointFactory factory = previewView.getMeteringPointFactory();
                   MeteringPoint point = factory.createPoint(event.getX(), event.getY());
                   FocusMeteringAction action = new FocusMeteringAction.Builder(point).build();
                   camera.getCameraControl().startFocusAndMetering(action);
                   return true;
               }
               else return false;
           }
       });
    }

    @SuppressLint({"ClickableViewAccessibility", "RestrictedApi"})
    private void SetUpCamera(ProcessCameraProvider cameraProvider) {

        // Connect camera preview to View
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        // Set up video capture
        @SuppressLint("RestrictedApi")
        VideoCapture videoCapture = new VideoCapture.Builder()
                .setTargetRotation(previewView.getDisplay().getRotation())
                .setVideoFrameRate(30)
                .setDefaultResolution(new Size(1920, 1080))
                .setMaxResolution(new Size(1920, 1080))
                .build();

        // Binds use cases
        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, videoCapture);

        // Output directory: Android/media/com.example.smarteye_app
        File file = new File(getExternalMediaDirs()[0].toString(), "EYE.mp4");
        Executor exec = ContextCompat.getMainExecutor(this);
        VideoCapture.OutputFileOptions outputFileOptions = new VideoCapture.OutputFileOptions.Builder(file).build();

        captureBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint({"RestrictedApi", "UnsafeExperimentalUsageError"})
            @Override
            public void onClick(View v) {
                // Start recording
                videoCapture.startRecording(outputFileOptions, exec, new VideoCapture.OnVideoSavedCallback() {

                    @Override
                    public void onVideoSaved(@NonNull VideoCapture.OutputFileResults outputFileResults) {
                        //Start processing
                        formActivity(v);
                    }

                    @Override
                    public void onError(int videoCaptureError, @NonNull String message, @Nullable Throwable cause) {
                        Toast.makeText(CameraActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();

                    }
                });
                /**
                @SuppressLint("UnsafeExperimentalUsageError") Range<Integer> range = camera.getCameraInfo().getExposureState().getExposureCompensationRange();
                Log.i("CAMERADEB", "com low " + range.getLower() + " comp high " + range.getUpper());
                camera.getCameraControl().setExposureCompensationIndex(range.getLower());
                */
                // Hide camera button and show green circle while recording
                captureBtn.setVisibility(View.INVISIBLE);
                red_circle.setVisibility(View.INVISIBLE);
                green_circle.setVisibility(View.VISIBLE);

                // Wait before turning on flashlight.
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @SuppressLint("UnsafeExperimentalUsageError")
                    @Override
                    public void run() {
                        camera.getCameraControl().enableTorch(true);
                    }
                }, FLASHLIGHT_DELAY);

                // Stop recording after set delay
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        videoCapture.stopRecording();
                        camera.getCameraControl().enableTorch(false);
                    }
                }, RECORDING_LENGTH);
            }
        });

        tapToFocus(camera);
    }

    public void deleteVideo() {
        File file1 = new File(getExternalMediaDirs()[0].toString() + "/EYE2.mp4");
        File file2 = new File(getExternalMediaDirs()[0].toString() + "/EYE.mp4");
        boolean isSuccess1 = file1.delete();
        boolean isSuccess2 = file2.delete();
    }
}