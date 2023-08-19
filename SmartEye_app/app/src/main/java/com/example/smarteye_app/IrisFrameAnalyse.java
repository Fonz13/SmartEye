package com.example.smarteye_app;

import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class IrisFrameAnalyse {

	private int resolutionH = 1080;
	private int resolutionW = 1920;
	private int target = 350;

	private double irisSize;
	private double pupilSize;
	private double oldIris;

	public IrisFrameAnalyse() {
		Log.i("IrisFrameAnalyse", "INIT: ");

	}

	public double getIrisSize(){
		if(irisSize == 0){
			return oldIris;
		}
		return irisSize;
	}

	public double getPupilSize(){
		return pupilSize;
	}


	public double analyse(Mat frame, double oldiris, int frameNr) {
		//Rect roi = findRoi(frame);
		Rect roi = new Rect((resolutionW / 2) - (target / 2), (resolutionH / 2) - (target / 2), target, target);
		if(oldiris == 0){
			oldiris = target - 100;
		}
		try {
			pupilSize = findPupil(frame, roi, oldiris, frameNr);
		} catch (Exception e) {
			Log.i("IrisFrameAnalyse", e.getMessage());
		}
		double irisSize = findIris(frame, roi, frameNr);
		oldIris = oldiris;
		Log.i("IrisDeb", "oldIris " + String.valueOf(oldiris));
		Log.i("IrisDeb", "oldIris " + String.valueOf(irisSize));
		if(irisSize == 0 & oldiris == 0){
			irisSize = target - 100;
		}
		if(oldiris != 0 & irisSize == 0){
			irisSize = oldiris;
		}
		double relation = pupilSize/irisSize;
		//Log.i("IrisDeb", "oldIris " + String.valueOf(oldiris));
		Log.i("IrisDeb", "Pupil/iris relation: " +"pupil " + String.valueOf(pupilSize) + "Iris "+ String.valueOf(irisSize));
		Log.i("IrisDebrel", String.valueOf(relation));
		return relation;
	}

	private double findPupil(Mat img, Rect roi, double irisSize, int frameNr) throws Exception {
		org.opencv.core.Point centerIris = new org.opencv.core.Point();
		float[] radiiIris = new float[1];

		//Imgproc.drawContours(img, contours, -1, new Scalar(0,0,255));
		//Imgproc.rectangle(img, roi, new Scalar(0,255,0));

		//returns if roi is out of bounds. TODO Exceptions
		if(roi.x < 0 || roi.y < 0 || roi.height + roi.y > img.size(0) || roi.width + roi.x > img.size(1)) {throw new Exception("NoRoi");}
		Mat gray = new Mat();
		Imgproc.cvtColor(img, gray, Imgproc.COLOR_BGR2GRAY);
		Mat imgRoi = new Mat(gray, roi);
		if(imgRoi.empty()){throw new Exception("NoRoi");}

		Imgproc.GaussianBlur(imgRoi, imgRoi, new Size(7,7), 0);
		//debug_roiBlurred = imgRoi.clone();

		//TODO fails when contrast is too high
		//Sets contrast of image to filter out the pupil from the iris

		if(frameNr > 13 & frameNr < 16){
			imgRoi.convertTo(imgRoi, -1, 1, -100);
		} else {
			imgRoi.convertTo(imgRoi, -1, 6, 0);
			imgRoi.convertTo(imgRoi, -1, 6, 0);
		}




		//Masks out an area in the original img with our roi
		Mat imgRoiC = new Mat(img, roi);

		//Threshold for detecting the pupil. This is under testing, otsu with a different contrast for different imgs seems optimal.
		Mat threshRoi = new Mat();
		//Imgproc.adaptiveThreshold(imgRoi, thresh, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 3, 1.7);
		double t = Imgproc.threshold(imgRoi, threshRoi, 0, 255, Imgproc.THRESH_OTSU);
		Log.i("RESULT", " fn"+ frameNr+ " t"+ t);
		//Imgproc.Canny(imgRoi, thresh, 20, 40);

		//debug_roiThresh = threshRoi;

		//Find contours for the pupil, uses the same filtering technique as for the iris but with the added filter of checking that the circle is a certain size.
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat hierarchy = new Mat();
		Imgproc.findContours(threshRoi, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_TC89_L1);
		hierarchy.release();
		for(int i = 0; i < contours.size(); i++) {
			//System.out.println("Detected some contours");

			MatOfPoint contour = contours.get(i);

			if(
							Imgproc.contourArea(contour) > ( ((0.15*(irisSize/2))*(0.15*(irisSize/2))) *Math.PI)
							&&
							Imgproc.contourArea(contour) < ( ((0.75*(irisSize/2))*(0.75*(irisSize/2))) *Math.PI)
			) {
				Log.i("PUPIL", "Found area: " + Imgproc.contourArea(contour));
				//Smooths the contour to make it more circleish
				MatOfPoint mopIn = contour;
				MatOfInt hull = new MatOfInt();
				Imgproc.convexHull(mopIn, hull, false);

				MatOfPoint mopOut = new MatOfPoint();
				mopOut.create((int)hull.size().height,1,CvType.CV_32SC2);

				for(int j = 0; j < hull.size().height ; j++)
				{
					int index = (int)hull.get(j, 0)[0];
					double[] point = new double[] {
							mopIn.get(index, 0)[0], mopIn.get(index, 0)[1]
					};
					mopOut.put(j, 0, point);
				}

				//Filters a second time and then makes a minRadii circle to use as a measurement for the iris
				MatOfPoint2f filteredConts = new MatOfPoint2f(mopOut.toArray());
				RotatedRect rectContour = Imgproc.minAreaRect(filteredConts);
				if(
						rectContour.size.width*(1-5*0.02) > rectContour.size.height
								||
								rectContour.size.height*(1-5*0.02) > rectContour.size.width
				) {
					//System.out.println("rect removed: " + rectContour.size);
					continue;
				}

				//Filtering assumed done now calculates iris size with a min radii circle

				Imgproc.minEnclosingCircle(filteredConts, centerIris, radiiIris);


				if(radiiIris[0]*2 >= pupilSize) {
					//System.out.println("Size of iris is: " + radiiIris[0]*2 + " pixels" );
					pupilSize = (double) (radiiIris[0]*2);

				}else {
					continue;
				}
			}
		}
		Imgproc.circle(imgRoiC, centerIris, (int) radiiIris[0], new Scalar(0,255,0));
		//debug_roiC = imgRoiC;
		Log.i("Iris", "Pupil size: " + String.valueOf(pupilSize));
		//Imgproc.putText(imgRoiC, (int) pupilSize + " pixels", new org.opencv.core.Point((int) centerIris.x - 100, (int) (centerIris.y + radiiIris[0]*1.5) ), Imgproc.FONT_HERSHEY_SIMPLEX, 4, new Scalar(0,255,0));
		//Imgproc.drawContours(imgRoiC, contours, -1, new Scalar(0,0,255));
		imgRoiC.copyTo(img.submat(roi));
		//debug_img = img;
		return pupilSize;
	}


	private double findIris(Mat img, Rect roi, int frameNr){
		boolean ret = false;
		Mat gray = new Mat();
		Mat roiC = new Mat(img, roi);

		Imgproc.cvtColor(roiC, gray, Imgproc.COLOR_BGR2GRAY);
		Mat blurred = new Mat();
		Imgproc.GaussianBlur(gray, blurred, new Size(5,5), 0);
		//debug_blurred = blurred;


		Mat temp = new Mat();
		//blurred.convertTo(temp, -1, 1, -50);
		blurred.convertTo(temp, -1, 1, -95);



		//debug_contrast = temp.clone();
		Mat thresh = new Mat();
		double calcthresh = Imgproc.threshold(temp, thresh, 0, 255, Imgproc.THRESH_OTSU);

		//System.out.println("First thresh filter= " + calcThresh);
		//Imgproc.threshold(temp, thresh, (double) threshold, 255.0, Imgproc.THRESH_BINARY);
		//debug_thresh = thresh;

		//Find contours
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat hierarchy = new Mat();
		Imgproc.findContours(thresh, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_TC89_L1);
		hierarchy.release();

		//Parses through contours detected, each index is a coherent contour see https://docs.opencv.org/3.4/dd/d49/tutorial_py_contour_features.html
		for(int i = 0; i < contours.size(); i++) {
			//System.out.println("Detected some contours");

			MatOfPoint contour = contours.get(i);
			//System.out.println("Actual area: " + Imgproc.contourArea(contour));

			//if(contour.width() < (roi.width-100)*0.7 || contour.width() > (roi.width-100)*1.3) continue;

			//Smooths the contour to make it more circleish
			MatOfPoint mopIn = contour;
			MatOfInt hull = new MatOfInt();
			Imgproc.convexHull(mopIn, hull, false);

			MatOfPoint mopOut = new MatOfPoint();
			mopOut.create((int)hull.size().height,1,CvType.CV_32SC2);

			for(int j = 0; j < hull.size().height ; j++)
			{
				int index = (int)hull.get(j, 0)[0];
				double[] point = new double[] {
						mopIn.get(index, 0)[0], mopIn.get(index, 0)[1]
				};
				mopOut.put(j, 0, point);
			}

			//Filters a second time by comparing width and height, a perfect circle should be equal. Then makes a minRadii circle to use as a measurement
			MatOfPoint2f filteredConts = new MatOfPoint2f(mopOut.toArray());
			RotatedRect rectContour = Imgproc.minAreaRect(filteredConts);
			if(
					rectContour.size.width*(1-5*0.01) > rectContour.size.height
							||
							rectContour.size.height*(1-5*0.01) > rectContour.size.width
			) {
				//System.out.println("rect removed: " + rectContour.size);
				continue;
			}


			//Filtering assumed done now calculates iris size with a min radii circle
			org.opencv.core.Point center = new org.opencv.core.Point();
			float[] radii = new float[1];
			Imgproc.minEnclosingCircle(filteredConts, center, radii);

			//For showing circles
			//Imgproc.circle(img, center, (int) radii[0], new Scalar(0,255,0));
			if(radii[0]*2 < (target-100)*0.6){ continue;}
			if(center.x + radii[0] > target || center.y + radii[0] > target || center.x -radii[0] < 0 || center.y - radii[0] < 0){ continue;}
			if(radii[0]*2 >= irisSize && radii[0] > 0) {
				irisSize = (double) (radii[0]*2);
				Log.i("IrisD", "Iris size: " + String.valueOf(radii[0]*2));
				Imgproc.circle(roiC, center, (int) radii[0], new Scalar(255,0,0));
			}
		}
		roiC.copyTo(img.submat(roi));
		//Imgproc.drawContours(img, contours, -1, new Scalar(0,255,0));
		return irisSize;
	}



}
