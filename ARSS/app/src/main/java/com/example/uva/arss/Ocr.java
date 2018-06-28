/*
    This file contains the sourcecode for Optical Character Recognition (OCR) implementation. This
    uses OpenCV for image processing. The processed image is fed into a Google Vision SDK, called
    Firebase. The image is first preprocessed by applying a gaussian blur and adaptive tresholding.

    After this, the largest four-sided polygon is found. Then the perspective is warped such that
    the found sudoku is the only thing displayed. This is sent to the Firebase SDK for processing.

    A lot of credit goes towards this github repo for being both a source of inspiration and code:
    https://github.com/joseluisdiaz/sudoku-solver/tree/master/src/main/java/org/losmonos/sudoku/grabber
 */
package com.example.uva.arss;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.NonNull;

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
    int height, width, index;
    Bitmap input, sudoku;
    Mat mat, hierarchy, matF, matT, wrapped, original;
    Size FOUR_CORNERS = new Size(1, 4);
    int[] grid = new int[81];

    // Constructors.
    public Ocr(Bitmap bitmap) {
        height = bitmap.getHeight();
        width = bitmap.getWidth();
        input = bitmap;
    }

    public Ocr() {
    }

    // Sets the Mat element.
    public void setMat(Mat mat) {
        this.mat = mat;
        this.original = mat;
        height = mat.height();
        width = mat.width();
    }


    // This function finds the grid using camera or gallery input. The image is preprocessed, then
    // the largest polygon is found and warped to fit the whole image.
    public Mat findGrid() {
        mat = turnImg(mat);
        // Preprocessing the incoming image.
        mat = preProcessMat(mat);

        // Largest polygon is found.
        MatOfPoint largest = largestPolygon(mat);

        if(largest != null) {
            MatOfPoint2f aproxPolygon = aproxPolygon(largest);
            // Only four-sided polygons are allowed, sudoku's are squares.
            if(Objects.equals(aproxPolygon.size(), FOUR_CORNERS)) {
                // The image is warped to be a square instead of tilted because of perspective.
                int size = distance(aproxPolygon);
                Mat cutted = applyMask(original, largest);
                wrapped = wrapPerspective(size, orderPoints(aproxPolygon), cutted);
                Imgproc.resize(wrapped, wrapped, new Size(original.cols(), original.rows()));
                sudoku = Bitmap.createBitmap(wrapped.width(), wrapped.height(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(wrapped, sudoku);
                // The image is split into 81 parts.
                List<Mat> cells = getBoxes(wrapped);
                Imgproc.resize(cells.get(0), cells.get(0), new Size(original.cols(), original.rows()));
                return wrapped;
            }
        }
        return mat;
    }

    // This function starts the OCR by splitting the grid in 81 parts and sending them to the Firebase
    // SDK for processing and recognition.
    public int[] startRecog() {
        List<Mat> cells = getBoxes(wrapped);
        for (int i = 0; i < cells.size(); i++) {
            Mat cell = cells.get(i);
            sudoku = Bitmap.createBitmap(cell.height(), cell.width(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(cell, sudoku);
            recognizeText(i);
        }
        return grid;
    }

    // This function preprocesses the image using gaussian blurring and adaptive tresholding.
    // This makes lines and polygons more prominent and distinguished in the image.
    private Mat preProcessMat(Mat preprocMat) {
        Mat processed = new Mat(preprocMat.size(), CV_8UC4);
        Imgproc.cvtColor(preprocMat, processed, Imgproc.COLOR_RGBA2GRAY);
        Imgproc.GaussianBlur(processed, processed, new Size(11, 11), 0);
        Imgproc.adaptiveThreshold(processed, processed, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 5, 2);
        Core.bitwise_not(processed, processed);
        return processed;
    }

    // This function turns a image since the input image is rotated 90 degrees.
    private Mat turnImg(Mat original) {
        matF = new Mat(mat.height(), mat.width(), CV_8UC4);
        matT = new Mat(mat.width(), mat.width(), CV_8UC4);
        Core.transpose(original, matT);
        Imgproc.resize(matT, matF, matF.size(), 0, 0, 0);
        Core.flip(matF, original, 1);
        return original;
    }


    // This function finds the largest polygon using findContours and sorting for the largest.
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

    // THis funcion aproximates polygons.
    private MatOfPoint2f aproxPolygon(MatOfPoint poly) {
        MatOfPoint2f dst = new MatOfPoint2f();
        MatOfPoint2f src = new MatOfPoint2f();
        poly.convertTo(src, CvType.CV_32FC2);

        double arcLength = Imgproc.arcLength(src, true);
        approxPolyDP(src, dst, 0.02 * arcLength, true);

        return dst;
    }

    // This function calculates the size of a polygon.
    private int distance(MatOfPoint2f poly) {
        org.opencv.core.Point[] a =  poly.toArray();
        return (int)Math.sqrt((a[0].x - a[1].x)*(a[0].x - a[1].x) +
                (a[0].y - a[1].y)*(a[0].y - a[1].y));
    }

    // This function applies a mask  on an image.
    private Mat applyMask(Mat image, MatOfPoint poly) {
        Mat mask = Mat.zeros(image.size(), CvType.CV_8UC1);

        Imgproc.drawContours(mask, ImmutableList.of(poly), 0, Scalar.all(255), -1);
        Imgproc.drawContours(mask, ImmutableList.of(poly), 0, Scalar.all(0), 2);

        Mat dst = new Mat();
        image.copyTo(dst, mask);

        return dst;
    }

    // This function warps an image to fit the screen regardless of perspective. This is done using
    // a transformation matrix.
    private Mat wrapPerspective(int size, MatOfPoint2f src, Mat image) {
        Size reshape = new Size(size, size);

        Mat undistorted = new Mat(reshape, CV_8UC4);

        MatOfPoint2f d = new MatOfPoint2f();
        d.fromArray(new org.opencv.core.Point(0, 0), new org.opencv.core.Point(0, reshape.width), new org.opencv.core.Point(reshape.height, 0),
                new org.opencv.core.Point(reshape.width, reshape.height));

        // Warping the perspective.
        warpPerspective(image, undistorted, getPerspectiveTransform(src, d), reshape);

        return undistorted;
    }

    // This function orders the points of a polygon for the warping of an image.
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

    // This function splits an image into 81 pieces since a sudoku has 81 cells.
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

    // This function recognizes text using Firebase, through a bitmap image.
    public void recognizeText(int i) {
        index = i;
        // Image input.
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(sudoku);
        FirebaseVisionTextDetector detector = FirebaseVision.getInstance().getVisionTextDetector();
        Task<FirebaseVisionText> result =
                detector
                .detectInImage(image).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                @Override
                // If something is found.
                public void onSuccess(FirebaseVisionText firebaseVisionText) {
                    for (FirebaseVisionText.Block block: firebaseVisionText.getBlocks()) {
                        Rect boundingBox = block.getBoundingBox();
                        Point[] cornerPoints = block.getCornerPoints();
                        String text = block.getText();
                        // Parsing the string to integers to insert into grid.
                        try {
                            grid[index] = Integer.parseInt(text);
                        }
                        catch (Exception e) {
                            grid[index] = 0;
                        }
                    }
                }
            })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                            }
                        }
                );
    }
}
