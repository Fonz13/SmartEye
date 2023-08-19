package com.example.smarteye_app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadata;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;


import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.badoo.mobile.util.WeakHandler;
//https://stackoverflow.com/questions/4369537/update-ui-from-thread-in-android

//Oavsett input sparar den våra sparade roterade. videos från min kamera sparas inte roterade???
public class ProcessActivity extends AppCompatActivity {


    public TextView encodedFrames;
    public TextView processedFrames;
    public TextView totalFrames;
    public TextView pupilsFound;
    public ProgressBar pBar1;
    int numProcessedFrames;
    int pupils = 0;
    double largestPupil = 0;
    int largestPupilNr = 0;
    double smallestPupil = 1;
    int smallestPupilNr = 0;
    double time;
    double sizedifference;
    private int intTotalFrames;
    private ProgressBar progress;

    int hit = 0;

    private boolean status = false;
    double fps = 30;
    private double oldIris;

    ExecutorService executor = Executors.newSingleThreadExecutor();
    WeakHandler handler = new WeakHandler(Looper.getMainLooper());
    BitmapToVideoEncoder bitmapToVideoEncoder;


    List<Double> relativePupilSizes;


    //inits opencv library
    static {
        if (!OpenCVLoader.initDebug())
            Log.d("ERROR", "Unable to load OpenCV");
        else
            Log.d("SUCCESS", "OpenCV loaded");
    }

    public boolean getDone(){
        return status;
    }
    public double getSizedifference(){
        return sizedifference;
    }

    public double getTime(){
        return time;
    }

    public double getSmallestPupil(){
        return smallestPupil;
    }
    public double getLargestPupil(){
        return largestPupil;
    }

    public void getFrames(String filename, String output) throws Exception {
        //Get amounts of frame count in video
        MediaMetadataRetriever mmr;
        mmr = new MediaMetadataRetriever();
        mmr.setDataSource(filename + "/EYE.mp4");
        String strTotalFrames = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT);
        intTotalFrames = Integer.parseInt(strTotalFrames) + 1; //mmr is zero-based indexed, ie one frame less then real

        relativePupilSizes = new ArrayList<Double>();
        bitmapToVideoEncoder = new BitmapToVideoEncoder(new BitmapToVideoEncoder.IBitmapToVideoEncoderCallback() {
            @Override
            public void onEncodingComplete(File outputFile) {
                Log.i("ENC", "onEncodingComplete: COMPLETE");
            }
        });

        numProcessedFrames = 0;
        //INITIATES VIDEOCAPTURE
        VideoCapture cap = new VideoCapture();
        //CONSTRUCTS VIDEOCAPTURE
        cap.open(filename+"/EYE.mp4");
        boolean success = true;

        //READS FIRST FRAME
        Mat frame = new Mat();
        cap.read(frame);
        //CONSTUCTS ENCODER

        bitmapToVideoEncoder.startEncoding(frame.cols(), frame.rows(), new File(filename + "/EYE2.mp4"), handler);


        //WRITES FIRST FRAME
        processFrame(frame);


        while (success) {
            success = cap.read(frame);
            // check if we succeeded
            if (frame.empty()) {
                success = false;
                Log.i("VideoTest", "FAILED: " + numProcessedFrames);
            } else {
                Log.i("VideoTest", "SUCESS: " + numProcessedFrames);
                /////
                //Imgcodecs.imwrite(getExternalMediaDirs()[0].toString()+"/"+count+".png", frame);
                processFrame(frame);
            }
        }


        ////
        bitmapToVideoEncoder.stopEncoding();
        Log.i("VideoTest", "VIDEO COMPELE: ");

        getRetractTime(relativePupilSizes, 0.92);
        //releases all mats
        frame.release();
    }

    public void setProggressBar(ProgressBar v){
        progress = v;
    }

    private void processFrame(Mat frame) throws Exception {

        IrisFrameAnalyse mesure = new IrisFrameAnalyse();
        double val = mesure.analyse(frame, oldIris, numProcessedFrames);
        relativePupilSizes.add(val);
        oldIris = mesure.getIrisSize();
        //to Bitmap
        Bitmap bmFrame = Bitmap.createBitmap(frame.cols(), frame.rows(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(frame, bmFrame);
        //from thread to ui
        handler.post(new Runnable() {
            @Override
            public void run() {
                progress.setProgress((int) ((intTotalFrames/numProcessedFrames)*100) );
            }
        });
        numProcessedFrames++;
        //queues to encoder
        bitmapToVideoEncoder.queueFrame(bmFrame);
    }

    /*
    @Override
    public void onBackPressed() {
        bitmapToVideoEncoder.abortEncoding();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        ;
    }**/

    private void getRetractTime(List<Double> pupils, double smallAccSens) {
        hit = 0;
        double largeAcc = 0;
        double smallAcc = 1;
        double v1 = 1;
        double v2 = 1;
        //double v3 = 1;

        int largeAccFNr = 0;
        int smallAccFNr = 0;
        double firstsequenceEnd =  fps*1;
        double firstsequenceStart = fps*0.3;

        for (int i = 0; i < pupils.size(); i++) {
            double val = pupils.get(i);
            Log.i("Resultl" , " at" +i+ " "+ val);
            if (i < firstsequenceEnd & i > firstsequenceStart & val > (largeAcc * 1.01) & val < 1) {
                largeAcc = val;
                largeAccFNr = i;
            }
            //double avg = (v1+v2+v3)/3;
            double avg = (v1+v2)/2;
            double comp = avg;
            if(v1 > 0.9*avg && v1 < 1.1 *avg) comp = v1;
            if (i > (fps*0.5) & val > 0) {
                hit++;
                Log.i("RESULTAVG", "VAl=" + val + " avg =" + comp);
                if (val < comp * smallAccSens){
                    smallAcc = val;
                    smallAccFNr = i;
                }
            v2 = v1;
            v1 = val;
            }
        }
        sizedifference = largeAcc- smallAcc;
        if(sizedifference < 0){
            sizedifference = 0;
        }
        time = (smallAccFNr - ((fps*0.5)-1))/fps;
        if(time < 0) time = 0;
        if( smallAcc < 1) {
            smallestPupil = smallAcc;
        } else smallestPupil = 0;
        if(largeAcc > 0 & largeAcc > smallestPupil){
            largestPupil = largeAcc;
        }

        if(smallAcc == 1 & smallAccSens > 1) getRetractTime(pupils, smallAccSens+0.01);
        Log.i("Result", "large " + largeAcc +" at frame " + largeAccFNr +" Smallframe " + smallAcc + " at frame " + smallAccFNr);
        Log.i("Result", "Size diff "+ sizedifference+ " and time " + time + " seconds" + "sens" + smallAccSens);
        status = true;
    }

    public double getHitPercent(){
        double hitPercent = hit/(intTotalFrames- ((double) fps*0.5));
        return hitPercent;
    }

}

