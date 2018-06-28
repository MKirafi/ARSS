package com.example.uva.arss;

import android.graphics.Bitmap;

public class Ocr {
    int height, width;
    public Ocr(Bitmap bitmap) {
        height = bitmap.getHeight();
        width = bitmap.getWidth();
    }
}
