package com.example.uva.arss;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Picture;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.Photo;

import java.util.ArrayList;
import java.util.List;

import static org.opencv.core.CvType.CV_8UC1;
import static org.opencv.core.CvType.CV_8UC4;
import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_SIMPLE;
import static org.opencv.imgproc.Imgproc.RETR_EXTERNAL;
import static org.opencv.imgproc.Imgproc.RETR_LIST;
import static org.opencv.imgproc.Imgproc.contourArea;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    CameraBridgeViewBase camera;
    Mat mat, matF, matT, hierarchy;
    BaseLoaderCallback baseLoaderCallback;
    List<MatOfPoint> contours = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermission();

        if(OpenCVLoader.initDebug()) {
            Toast.makeText(getApplicationContext(), "loaded", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(getApplicationContext(), "not loaded", Toast.LENGTH_SHORT).show();
        }

        camera = (JavaCameraView) findViewById(R.id.myCameraView);
        camera.setVisibility(SurfaceView.VISIBLE);
        camera.setCvCameraViewListener(this);

        baseLoaderCallback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                switch (status) {
                    case BaseLoaderCallback.SUCCESS:
                        camera.enableView();
                        break;
                    default:
                        super.onManagerConnected(status);
                        break;

                }
            }
        };
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mat = inputFrame.rgba();
        hierarchy = new Mat();
        Core.transpose(mat, matT);
        Imgproc.resize(matT, matF, matF.size(), 0, 0, 0);
        Core.flip(matF, mat, 1);
          Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGBA2GRAY);
          Imgproc.GaussianBlur(mat, mat, new Size(11, 11), 0);
          Imgproc.adaptiveThreshold(mat, mat, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 5, 2);
          Core.bitwise_not(mat, mat);
          Imgproc.Canny(mat, mat, 400, 600, 5, true);
          Imgproc.findContours(mat.clone(), contours, hierarchy,RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);

          double largest_area = 0;
          int largest_area_index = 0;
          Mat largest_contour = new Mat();
          for(int i = 0; i < contours.size(); i++) {
              double size = contourArea(contours.get(i), false);
            if(size > largest_area) {
                largest_area = size;
                largest_area_index = i;
                largest_contour = contours.get(i);
            }
          }
          System.out.println("Rows: " + largest_contour.rows());
          System.out.println("Columns: " + largest_contour.cols());
        return mat;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mat = new Mat(height, width, CV_8UC4);
        matF = new Mat(height, width, CV_8UC4);
        matT = new Mat(width, width, CV_8UC4);
    }

    @Override
    public void onCameraViewStopped() {
        mat.release();
        matF.release();
        matT.release();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(camera != null) {
            camera.disableView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!OpenCVLoader.initDebug()) {
            Toast.makeText(getApplicationContext(), "Opencv problem", Toast.LENGTH_LONG).show();
        }
        else {
            baseLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(camera != null) {
            camera.disableView();
        }
    }

    private void checkPermission(){
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        }
    }

}
