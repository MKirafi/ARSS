// https://github.com/joseluisdiaz/sudoku-solver/blob/master/src/main/java/org/losmonos/sudoku/grabber/DetectSudoku.java

package com.example.uva.arss;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;
import org.opencv.ml.KNearest;

import static org.opencv.core.CvType.CV_8U;
import static org.opencv.imgcodecs.Imgcodecs.CV_LOAD_IMAGE_ANYDEPTH;
import static org.opencv.imgcodecs.Imgcodecs.CV_LOAD_IMAGE_COLOR;
import static org.opencv.imgproc.Imgproc.moments;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.opencv.core.CvType.CV_8UC4;
import static org.opencv.imgcodecs.Imgcodecs.imread;
import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_SIMPLE;
import static org.opencv.imgproc.Imgproc.RETR_EXTERNAL;
import static org.opencv.imgproc.Imgproc.approxPolyDP;
import static org.opencv.imgproc.Imgproc.contourArea;
import static org.opencv.imgproc.Imgproc.getPerspectiveTransform;
import static org.opencv.imgproc.Imgproc.warpAffine;
import static org.opencv.imgproc.Imgproc.warpPerspective;

import static com.example.uva.arss.FeatureDetector.CONTAIN_DIGIT_SUB_MATRIX_DENSITY;
import static org.opencv.ml.Ml.ROW_SAMPLE;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    CameraBridgeViewBase camera;
    Mat mat, matF, matT, hierarchy, mat2;
    BaseLoaderCallback baseLoaderCallback;
    Size FOUR_CORNERS = new Size(1, 4);
    final int SZ = 20;
    KNearest knn;

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

//        camera = (JavaCameraView) findViewById(R.id.myCameraView);
//        camera.setVisibility(SurfaceView.VISIBLE);
//        camera.setCvCameraViewListener(this);

        baseLoaderCallback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                switch (status) {
                    case BaseLoaderCallback.SUCCESS:
//                        camera.enableView();
                        break;
                    default:
                        super.onManagerConnected(status);
                        break;
                }
            }
        };

        ImageButton btn_choose_photo = (ImageButton) findViewById(R.id.load_image_button); // Replace with id of your button.
        btn_choose_photo.setOnClickListener(btnChoosePhotoPressed);
    }

    public View.OnClickListener btnChoosePhotoPressed = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent i = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            final int ACTIVITY_SELECT_IMAGE = 1234;
            startActivityForResult(i, ACTIVITY_SELECT_IMAGE);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Uri imageUri = data.getData();
            try {
                Bitmap photo = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                int[] sudoku = recognizeSudoku(photo);
                if(sudoku != null) {
                    for (int i : sudoku) {
                        System.out.println("found: " + i);
                    }
                }
                else {
//                    Toast.makeText(getApplicationContext(), "Not found", Toast.LENGTH_LONG).show();
                }
            }
            catch(java.io.IOException e) {
                System.out.println("Something went wrong.");
            }

        }

    }

    public int[] recognizeSudoku(Bitmap input) {
        mat = new Mat();
        Bitmap bmp32 = input.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmp32, mat);

        mat = turnImg(mat);
        mat = preprocMat(mat);

        MatOfPoint largest = largestPolygon(mat);

        if(largest != null) {
//            MatOfPoint approxf1 = new MatOfPoint();
            MatOfPoint2f aproxPolygon = aproxPolygon(largest);

            if (Objects.equals(aproxPolygon.size(), FOUR_CORNERS)) {
                Toast.makeText(getApplicationContext(), "Got here!", Toast.LENGTH_LONG).show();
//                aproxPolygon.convertTo(approxf1, CvType.CV_32S);
//                List<MatOfPoint> contourTemp = new ArrayList<>();
//                contourTemp.add(approxf1);
//                Imgproc.drawContours(mat, contourTemp, 0, new Scalar(255, 255, 255), 20);

                int size = distance(aproxPolygon);

                Mat cutted = applyMask(mat, largest);

                Mat wrapped = wrapPerspective(size, orderPoints(aproxPolygon), cutted);
                List<Integer> sudoku = extractCells(wrapped);
                for (int i : sudoku) {
                    System.out.print("Value: " + i);
                }
                return convertIntegers(sudoku);
            }
        }
        return null;
    }

    public static int[] convertIntegers(List<Integer> integers)
    {
        int[] ret = new int[integers.size()];
        for (int i=0; i < ret.length; i++)
        {
            ret[i] = integers.get(i).intValue();
        }
        return ret;
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        mat2 = inputFrame.rgba();

//        mat = turnImg(mat);
//        mat = preprocMat(mat);
//
//        MatOfPoint largest = largestPolygon(mat);
//
//        if(largest != null) {
////            MatOfPoint approxf1 = new MatOfPoint();
//            MatOfPoint2f aproxPolygon = aproxPolygon(largest);
//
//            if (Objects.equals(aproxPolygon.size(), FOUR_CORNERS)) {
////                aproxPolygon.convertTo(approxf1, CvType.CV_32S);
////                List<MatOfPoint> contourTemp = new ArrayList<>();
////                contourTemp.add(approxf1);
////                Imgproc.drawContours(mat, contourTemp, 0, new Scalar(255, 255, 255), 20);
//
//                int size = distance(aproxPolygon);
//
//                Mat cutted = applyMask(mat, largest);
//
//                Mat wrapped = wrapPerspective(size, orderPoints(aproxPolygon), cutted);
//                List<Integer> sudoku = extractCells(wrapped);
//                for (int i : sudoku) {
//                    System.out.print("Value: " + i);
//                }
//                if(wrapped.rows() != mat.cols() || wrapped.cols() != mat.rows()) {
//                    Imgproc.resize(wrapped, wrapped, new Size(mat.cols(), mat.rows()));
//                }
//                return wrapped;
//            }
//        }
        return mat2;
    }

    private Mat turnImg(Mat original) {
        matF = new Mat(mat.height(), mat.width(), CV_8UC4);
        matT = new Mat(mat.width(), mat.width(), CV_8UC4);
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

    private List<Mat> getBoxes(Mat grid) {
        float size = (float)grid.rows()/(float)9;
        List<Mat> digitCells = Lists.newArrayList();
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                Rect rect = new Rect(new Point(col * size, row * size), new Size(size, size));
                Mat digit = new Mat(grid, rect).clone();
                digitCells.add(digit);
//                System.out.println(row * 9 + col);
            }
        }
        return digitCells;
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

    private List<Integer> extractCells(Mat m) {
        List<Mat> cells = getBoxes(m);
        List<Optional<Rect>> digitBoxes = Lists.transform(cells, FeatureDetector.GET_DIGIT_BOX_BYTE_SUM);

        List<Integer> result = Lists.newArrayList();
        List<Mat> cuts = Lists.newArrayList();
        /* zip... zip! :'( */

        for(int i = 0; i < cells.size(); i++ ) {
            Mat cell = cells.get(i);
            Optional<Rect> box = digitBoxes.get(i);

            int d = 0;

            if (box.isPresent() && CONTAIN_DIGIT_SUB_MATRIX_DENSITY.apply(cell)) {
                /* cut current cell to the finded box */
                Mat cutted = new Mat(cell, box.get()).clone();
                Imgproc.rectangle(cell, box.get().tl(), box.get().br(), Scalar.all(255));
                cuts.add(cutted);
                System.out.println("a: " + i);
                d = digitRecog(cutted);
            }

            Imgproc.rectangle(cell, new Point(0,0), new Point(100,100), Scalar.all(255));

            result.add(d);

        }

        return result;
    }

    // This function regocnizes a digit from an input cell using knearest neighbor and training
    // data.
    private Integer digitRecog(Mat cell) {
//        Mat warped = deskew(center(cell.clone()));
        Mat result = new Mat();

        loadTrainData();
        knn.findNearest(singleRowConvert(cell), 3, result);
        System.out.println("Value!!!!!!!!!!!!!!!!!: " + result.get(0, 0)[0]);
        return (int)result.get(0, 0)[0];
    }

    // This function loads the training data and creates the k-nearest neighbour model.
    private void loadTrainData() {
        Size digitSize = new Size(SZ, SZ);
        Mat trainData = new Mat();
        try {
            trainData = Utils.loadResource(getApplicationContext(), R.drawable.digits);
        }
        catch (Exception e) {
            System.out.println("Loading failed!!!!!!!!!!!" + e.getMessage());
            e.printStackTrace();
        }
//        System.out.println("trainData data: " + trainData.empty());
//        System.out.println("trainData cols: " + trainData.width());
//        System.out.println("trainData rows: " + trainData.height());
        int cols = trainData.width() / 20;
        int rows = trainData.height() / 20;
        int digitPerLabel = (cols * rows) / 10;

        Mat samples = Mat.zeros(cols * rows, SZ * SZ, CvType.CV_32FC1);
        Mat labels = Mat.zeros(cols * rows, 1, CvType.CV_32FC1);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Rect currentDigit = new Rect(new Point(j * SZ, i * SZ), digitSize);
                int index = i * cols + j;
                double label = (j + i * cols) / digitPerLabel;
                Mat cell = deskew(new Mat(trainData, currentDigit));
                Mat singleRow = singleRowConvert(cell);

                for (int k = 0; k < SZ * SZ; k++) {
                    samples.put(index, k, singleRow.get(0, k));
                }
                labels.put(index, 0, label);
            }
        }

        System.out.println("samples cols: " + samples.cols());
        System.out.println("samples rows: " + samples.rows());
        System.out.println("labels cols: " + labels.cols());
        System.out.println("labels rows: " + labels.rows());
        knn = KNearest.create();
        knn.setAlgorithmType(KNearest.KDTREE);
        knn.train(samples, ROW_SAMPLE, labels);
    }

    // This function converts a mat to a mat image with all data on a single row. This is for
    // the knn implementation since it takes every sample on a different row.
    private Mat singleRowConvert(Mat img) {
        Mat result = Mat.zeros(1, SZ * SZ, CvType.CV_32FC1);
        for (int row = 0; row < img.rows(); row++) {
            for (int col = 0; col < img.cols(); col++) {
                double data = img.get(row, col)[0] / 255.0;
                int index = SZ * row + col;
                result.put(0, index, data);
            }
        }
        return result;
    }

    // This function deskews skewed digits from the training data.
    public Mat deskew(Mat img) {
        Moments m = moments(img);

        if (Math.abs(m.get_mu02()) < 0.01) {
            return img.clone();
        }
        Mat result = new Mat(img.size(), CvType.CV_32FC1);
        double skew = m.get_mu11() / m.get_mu02();
        Mat M = new Mat(2, 3, CvType.CV_32FC1);

        M.put(0, 0, 1, skew, -0.5 * SZ * skew, 0, 1, 0);

        warpAffine(img, result, M, new Size(SZ, SZ), Imgproc.WARP_INVERSE_MAP | Imgproc.INTER_LINEAR);

        return result;
    }

    // This function centers digits.
    private Mat center(Mat digit) {
        Mat res = Mat.zeros(digit.size(), CvType.CV_32FC1);

        double s = 1.5*digit.height()/SZ;

        Moments m = moments(digit);

        double c1_0 = m.get_m10()/m.get_m00();
        double c1_1 = m.get_m01()/m.get_m00();

        double c0_0= SZ/2, c0_1 = SZ/2;

        double t_0 = c1_0 - s*c0_0;
        double t_1 = c1_1 - s*c0_1;

        Mat A = Mat.zeros( new Size(3, 2), CvType.CV_32FC1);

        A.put(0,0, s, 0, t_0);
        A.put(1,0, 0, s, t_1);

        warpAffine(digit, res, A, new Size(SZ, SZ), Imgproc.WARP_INVERSE_MAP | Imgproc.INTER_LINEAR);
        return res;
    }


    @Override
    public void onCameraViewStarted(int width, int height) {

        mat2 = new Mat(height, width, CV_8UC4);
        mat = new Mat();
    }

    @Override
    public void onCameraViewStopped() {
        mat2.release();
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
