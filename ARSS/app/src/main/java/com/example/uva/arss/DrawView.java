package com.example.uva.arss;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;


import java.util.jar.Attributes;


public class DrawView extends View {

    Paint paint = new Paint();
    int height, width;

    private void init(){
        paint.setColor(Color.BLACK);
    }

    public DrawView(Context context, int width, int height){
        super(context);
        this.setBackgroundColor(Color.TRANSPARENT);
        init();
        this.height = height;
        this.width = width;
    }

    public DrawView(Context context){
        super(context);
        this.setBackgroundColor(Color.TRANSPARENT);
        init();
    }

    public DrawView(Context context, AttributeSet attr){
        super(context, attr);
        this.setBackgroundColor(Color.TRANSPARENT);
        init();
    }

    public DrawView(Context context, AttributeSet attr, int defStyle){
        super(context, attr, defStyle);
        this.setBackgroundColor(Color.TRANSPARENT);
        init();
    }

    @Override
    public void onDraw(Canvas canvas){
        canvas.drawLine(width/11, 10, width - (width/11), height*9 + 10 , paint);
        canvas.drawLine(2 * width/11, 10, width - (width/11), height*9 + 10 , paint);
        canvas.drawLine(3 * width/11, 10, width - (width/11), height*9 + 10 , paint);
        canvas.drawLine(4 * width/11, 10, width - (width/11), height*9 + 10 , paint);
        canvas.drawLine(5 * width/11, 10, width - (width/11), height*9 + 10 , paint);
        canvas.drawLine(6 * width/11, 10, width - (width/11), height*9 + 10 , paint);
        canvas.drawLine(7 * width/11, 10, width - (width/11), height*9 + 10 , paint);
        canvas.drawLine(8 * width/11, 10, width - (width/11), height*9 + 10 , paint);
        canvas.drawLine(9 * width/11, 10, width - (width/11), height*9 + 10 , paint);
        canvas.drawLine(10 * width/11, 10, width - (width/11), height*9 + 10 , paint);
    }

}
