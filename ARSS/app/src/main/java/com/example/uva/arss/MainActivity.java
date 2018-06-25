// https://github.com/joseluisdiaz/sudoku-solver/blob/master/src/main/java/org/losmonos/sudoku/grabber/DetectSudoku.java

package com.example.uva.arss;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceView;
import android.widget.Toast;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.opencv.core.CvType.CV_8UC4;
import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_SIMPLE;
import static org.opencv.imgproc.Imgproc.RETR_EXTERNAL;
import static org.opencv.imgproc.Imgproc.approxPolyDP;
import static org.opencv.imgproc.Imgproc.contourArea;
import static org.opencv.imgproc.Imgproc.getPerspectiveTransform;
import static org.opencv.imgproc.Imgproc.warpPerspective;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    CameraBridgeViewBase camera;
    Mat mat, matF, matT, hierarchy;
    BaseLoaderCallback baseLoaderCallback;
    Size FOUR_CORNERS = new Size(1, 4);

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

        mat = turnImg(mat);
        mat = preprocMat(mat);

        MatOfPoint largest = largestPolygon(mat);

        if(largest != null) {
            MatOfPoint approxf1 = new MatOfPoint();
            MatOfPoint2f aproxPolygon = aproxPolygon(largest);

            if (Objects.equals(aproxPolygon.size(), FOUR_CORNERS)) {
                aproxPolygon.convertTo(approxf1, CvType.CV_32S);
                List<MatOfPoint> contourTemp = new ArrayList<>();
                contourTemp.add(approxf1);
                Imgproc.drawContours(mat, contourTemp, 0, new Scalar(255, 255, 255), 20);

                int size = distance(aproxPolygon);

                Mat cutted = applyMask(mat, largest);

                Mat wrapped = wrapPerspective(size, orderPoints(aproxPolygon), cutted);
                if(wrapped.rows() != mat.cols() || wrapped.cols() != mat.rows()) {
                    Imgproc.resize(wrapped, wrapped, new Size(mat.cols(), mat.rows()));
                }
                return wrapped;
            }
        }
        return mat;
    }

    private Mat turnImg(Mat original) {
//        Matrix matrix = new Matrix();
//        Mat rotated = new Mat(original.cols(), original.rows(), CV_8UC4);
//        matrix.setRotate(270);
//        matrix.postScale(-1, 1);
//        Bitmap bmp = Bitmap.createBitmap(original.cols(), original.rows(), Bitmap.Config.ARGB_8888);
//        Utils.matToBitmap(original, bmp);
//        Bitmap rotatedBmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
//        bmp.recycle();
//        Utils.bitmapToMat(rotatedBmp, rotated);
//        return rotated;
        Core.transpose(original, matT);
        Imgproc.resize(matT, matF, matF.size(), 0, 0, 0);
        Core.flip(matF, original, 1);
        return original;
    }

    private Mat preprocMat(Mat preprocMat) {
        Mat processed = new Mat(preprocMat.size(), CV_8UC4);
        Imgproc.cvtColor(preprocMat, processed, Imgproc.COLOR_RGBA2GRAY);
        Imgproc.GaussianBlur(processed, processed, new Size(11, 11), 0);
        Imgproc.adaptiveThreshold(processed, processed, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 5, 2);
        Core.bitwise_not(processed, processed);
        return processed;
    }

    private MatOfPoint largestPolygon(Mat mat) {
        double area, largestArea = 0;
        MatOfPoint largest = null;
        hierarchy = new Mat();
        List<MatOfPoint> contours = new ArrayList<>();

        Imgproc.findContours(mat.clone(), contours, hierarchy, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);

        for(int i = 0; i < contours.size(); i++) {
            area = contourArea(contours.get(i), false);
            if(area > largestArea) {
                largestArea = area;
                largest = contours.get(i);
            }
        }

        return largest;
    }

    private MatOfPoint2f aproxPolygon(MatOfPoint poly) {
        MatOfPoint2f dst = new MatOfPoint2f();
        MatOfPoint2f src = new MatOfPoint2f();
        poly.convertTo(src, CvType.CV_32FC2);

        double arcLength = Imgproc.arcLength(src, true);
        approxPolyDP(src, dst, 0.02 * arcLength, true);

        return dst;
    }

    private Mat applyMask(Mat image, MatOfPoint poly) {
        Mat mask = Mat.zeros(image.size(), CvType.CV_8UC1);

        Imgproc.drawContours(mask, ImmutableList.of(poly), 0, Scalar.all(255), -1);
        Imgproc.drawContours(mask, ImmutableList.of(poly), 0, Scalar.all(0), 2);

        Mat dst = new Mat();
        image.copyTo(dst, mask);

        return dst;
    }

    private int distance(MatOfPoint2f poly) {
        Point[] a =  poly.toArray();
        return (int)Math.sqrt((a[0].x - a[1].x)*(a[0].x - a[1].x) +
                (a[0].y - a[1].y)*(a[0].y - a[1].y));
    }

    private Mat cleanLines(Mat image) {
        Mat m = image.clone();
        Mat lines = new Mat();

        int threshold = 50;
        int minLineSize = 200;
        int lineGap = 20;

        Imgproc.HoughLinesP(m, lines, 1, Math.PI / 180, threshold, minLineSize, lineGap);

        for (int x = 0; x < lines.cols(); x++) {
            double[] vec = lines.get(0, x);
            double x1 = vec[0],
                    y1 = vec[1],
                    x2 = vec[2],
                    y2 = vec[3];
            Point start = new Point(x1, y1);
            Point end = new Point(x2, y2);

            Imgproc.line(m, start, end, Scalar.all(0), 3);
        }
        return m;
    }

    private Mat wrapPerspective(int size, MatOfPoint2f src, Mat image) {
        Size reshape = new Size(size, size);

        Mat undistorted = new Mat(reshape, CvType.CV_8UC4);

        MatOfPoint2f d = new MatOfPoint2f();
        d.fromArray(new Point(0, 0), new Point(0, reshape.width), new Point(reshape.height, 0),
                new Point(reshape.width, reshape.height));

        warpPerspective(image, undistorted, getPerspectiveTransform(src, d), reshape);

        return undistorted;
    }

    private MatOfPoint2f orderPoints(MatOfPoint2f mat) {
        List<Point> pointList = SORT.sortedCopy(mat.toList());

        if (pointList.get(1).x > pointList.get(2).x) {
            Collections.swap(pointList, 1, 2);
        }

        MatOfPoint2f s = new MatOfPoint2f();
        s.fromList(pointList);

        return s;
    }

    private static final Ordering<Point> SORT = Ordering.natural().nullsFirst().onResultOf(
            new Function<Point, Integer>() {
                public Integer apply(Point foo) {
                    return (int) (foo.x+foo.y);
                }
            }
    );

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
