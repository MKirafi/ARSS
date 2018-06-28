package com.example.uva.arss;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextDetector;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
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

public class Ocr {
    int height, width;
    Bitmap input, sudoku;
    Mat mat, hierarchy, matF, matT, wrapped, original;
    Size FOUR_CORNERS = new Size(1, 4);

    public Ocr(Bitmap bitmap) {
        height = bitmap.getHeight();
        width = bitmap.getWidth();
        input = bitmap;
    }

    public Ocr(Mat mat) {
        height = mat.height();
        width = mat.width();
        this.mat = mat;
        this.original = mat;
    }

    public Mat findGrid() {
        mat = turnImg(mat);
        mat = preProcessMat(mat);
        MatOfPoint largest = largestPolygon(mat);

        if(largest != null) {
            MatOfPoint2f aproxPolygon = aproxPolygon(largest);
            if(Objects.equals(aproxPolygon.size(), FOUR_CORNERS)) {
                int size = distance(aproxPolygon);
                Mat cutted = applyMask(original, largest);
                wrapped = wrapPerspective(size, orderPoints(aproxPolygon), cutted);
                Imgproc.resize(wrapped, wrapped, new Size(original.cols(), original.rows()));
                sudoku = Bitmap.createBitmap(wrapped.width(), wrapped.height(), Bitmap.Config.ARGB_8888);
                System.out.println(sudoku.getHeight() + " " + sudoku.getWidth());
                System.out.println(wrapped.height() + " " + wrapped.width());
                Utils.matToBitmap(wrapped, sudoku);
                recognizeText();
                return wrapped;
//                List<Mat> cells = getBoxes(wrapped);
//                for (Mat cell : cells) {
//                    sudoku = Bitmap.createBitmap(wrapped.height(), wrapped.width(), Bitmap.Config.ARGB_8888);
//                    Utils.matToBitmap(wrapped, sudoku);
//                    recognizeText();
//                }
            }
        }
        return mat;
    }

    public Bitmap recognizeSudoku() {
//        sudoku = input;
//        recognizeText();
        mat = new Mat(input.getHeight(), input.getWidth(), CV_8UC4);
        Bitmap bmp32 = input.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmp32, mat);

        mat = turnImg(mat);
        mat = preProcessMat(mat);
        MatOfPoint largest = largestPolygon(mat);

        if(largest != null) {
            MatOfPoint2f aproxPolygon = aproxPolygon(largest);
            if(Objects.equals(aproxPolygon.size(), FOUR_CORNERS)) {
                int size = distance(aproxPolygon);
                Mat cutted = mat;
               // Mat cutted = applyMask(mat, largest);
                Mat wrapped = wrapPerspective(size, orderPoints(aproxPolygon), cutted);
                if (wrapped == mat){
                    System.out.println("wrapped werkt nieeeeeeeeeeeeeeeeeeeeeet");
                }else{
                    System.out.println("wrapped werkt weeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeel");
                    System.out.println("Mat: " + mat.height() + " , " + mat.width());
                    System.out.println("Wrapped :" + wrapped.height() + " , " + wrapped.width());
                }
//                Core.bitwise_not(wrapped, wrapped);
//                Core.bitwise_not(wrapped, wrapped);
                sudoku = Bitmap.createBitmap(wrapped.height(), wrapped.width(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(wrapped, sudoku);
//                List<Mat> cells = getBoxes(wrapped);
//                for (Mat cell : cells) {
//                    sudoku = Bitmap.createBitmap(cell.height(), cell.width(), Bitmap.Config.ARGB_8888);
//                    Utils.matToBitmap(cell, sudoku);
                    recognizeText();
//                }
                return sudoku;

            }
//            else {
//                System.out.println("FAIL!!!");
//                System.out.println("FAIL!!!");
//                System.out.println("FAIL!!!");
//                System.out.println("FAIL!!!");
//                System.out.println("FAIL!!!");
//                System.out.println("FAIL!!!");
//            }
        }
        return null;
//        return new int[81];
    }

    private Mat preProcessMat(Mat preprocMat) {
        Mat processed = new Mat(preprocMat.size(), CV_8UC4);
        Imgproc.cvtColor(preprocMat, processed, Imgproc.COLOR_RGBA2GRAY);
        Imgproc.GaussianBlur(processed, processed, new Size(11, 11), 0);
        Imgproc.adaptiveThreshold(processed, processed, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 5, 2);
        Core.bitwise_not(processed, processed);
        return processed;
    }

    private Mat turnImg(Mat original) {
        matF = new Mat(mat.height(), mat.width(), CV_8UC4);
        matT = new Mat(mat.width(), mat.width(), CV_8UC4);
        Core.transpose(original, matT);
        Imgproc.resize(matT, matF, matF.size(), 0, 0, 0);
        Core.flip(matF, original, 1);
        return original;
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

    private int distance(MatOfPoint2f poly) {
        org.opencv.core.Point[] a =  poly.toArray();
        return (int)Math.sqrt((a[0].x - a[1].x)*(a[0].x - a[1].x) +
                (a[0].y - a[1].y)*(a[0].y - a[1].y));
    }

    private Mat applyMask(Mat image, MatOfPoint poly) {
        Mat mask = Mat.zeros(image.size(), CvType.CV_8UC1);

        Imgproc.drawContours(mask, ImmutableList.of(poly), 0, Scalar.all(255), -1);
        Imgproc.drawContours(mask, ImmutableList.of(poly), 0, Scalar.all(0), 2);

        Mat dst = new Mat();
        image.copyTo(dst, mask);

        return dst;
    }

    private Mat wrapPerspective(int size, MatOfPoint2f src, Mat image) {
        Size reshape = new Size(size, size);

        Mat undistorted = new Mat(reshape, CV_8UC4);

        MatOfPoint2f d = new MatOfPoint2f();
        d.fromArray(new org.opencv.core.Point(0, 0), new org.opencv.core.Point(0, reshape.width), new org.opencv.core.Point(reshape.height, 0),
                new org.opencv.core.Point(reshape.width, reshape.height));

        warpPerspective(image, undistorted, getPerspectiveTransform(src, d), reshape);

        return undistorted;
    }

    private MatOfPoint2f orderPoints(MatOfPoint2f mat) {
        List<org.opencv.core.Point> pointList = SORT.sortedCopy(mat.toList());

        if (pointList.get(1).x > pointList.get(2).x) {
            Collections.swap(pointList, 1, 2);
        }

        MatOfPoint2f s = new MatOfPoint2f();
        s.fromList(pointList);

        return s;
    }

    private static final Ordering<org.opencv.core.Point> SORT = Ordering.natural().nullsFirst().onResultOf(
            new Function<org.opencv.core.Point, Integer>() {
                public Integer apply(org.opencv.core.Point foo) {
                    return (int) (foo.x+foo.y);
                }
            }
    );

    private List<Mat> getBoxes(Mat grid) {
        int size = grid.rows()/9;
        List<Mat> digitCells = Lists.newArrayList();
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                org.opencv.core.Rect rect = new org.opencv.core.Rect(new org.opencv.core.Point(col * size, row * size), new Size(size, size));
                Mat digit = new Mat(grid, rect).clone();
                digitCells.add(digit);
            }
        }
        return digitCells;
    }


    private void recognizeText() {
//        System.out.println("rows: " + sudoku.getHeight() + " cols: " + sudoku.getWidth());
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(sudoku);
        FirebaseVisionTextDetector detector = FirebaseVision.getInstance().getVisionTextDetector();
        Task<FirebaseVisionText> result =
                detector
                .detectInImage(image).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                @Override
                public void onSuccess(FirebaseVisionText firebaseVisionText) {
                    System.out.println("FOUND SOMETHING!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    //println(firebaseVisionText.getBlocks().size());
                    for (FirebaseVisionText.Block block: firebaseVisionText.getBlocks()) {
                        Rect boundingBox = block.getBoundingBox();
                        Point[] cornerPoints = block.getCornerPoints();
                        String text = block.getText();
                        System.out.println("TEXT FOUND!!!!!!!!!!!!!!!!!!: " + text);

                        for (FirebaseVisionText.Line line: block.getLines()) {
                            // ...
                            for (FirebaseVisionText.Element element: line.getElements()) {
                                // ...
                                //System.out.println("Element: " + element.getText() + " bounding box " + element.getBoundingBox());
                            }
                        }
                    }
                }
            })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                System.out.println("FAIL!!!!!!!!!!!!!!!!!!!!! :(");
                                System.out.println("FAIL!!!!!!!!!!!!!!!!!!!!! :(");
                                System.out.println("FAIL!!!!!!!!!!!!!!!!!!!!! :(");
                                System.out.println("FAIL!!!!!!!!!!!!!!!!!!!!! :(");
                                System.out.println("FAIL!!!!!!!!!!!!!!!!!!!!! :(");
                            }
                        }
                );
    }
}
