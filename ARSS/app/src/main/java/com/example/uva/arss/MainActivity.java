package com.example.uva.arss;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;

import org.w3c.dom.*;
import android.content.ActivityNotFoundException;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.GridLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private IntentManager intentManager;
    private TextView txtOutput;
    private int CAMERA_REQUEST = 1888;
    private DrawView drawView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        Spinner language_spinner = (Spinner) findViewById(R.id.language_spinner);
        ArrayAdapter<CharSequence> languageAdapter = ArrayAdapter.
                    createFromResource(this, R.array.language_array, android.R.layout.simple_spinner_item);
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        language_spinner.setAdapter(languageAdapter);

        checkPermission();

        Button takeImage = findViewById(R.id.take_image_button);
        takeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, CAMERA_REQUEST);
            }
        });


        this.txtOutput = findViewById(R.id.txt_output);
        this.intentManager = new IntentManager();

        /* "record audio" button: */
        findViewById(R.id.record_audio).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivityForResult(intentManager.intent, 1);
                } catch (ActivityNotFoundException a) {
                    Toast.makeText(getApplicationContext(),
                            "No speech. No gain. Pls buy a new  phone.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

//        Paint paint = new Paint();
//        paint.setColor(Color.BLACK);
//        Canvas canvas = new Canvas();
//        canvas.drawLine(width/11, 10, width - (width/11), height*9 + 10 , paint);
//        canvas.drawLine(2 * width/11, 10, width - (width/11), height*9 + 10 , paint);
//        canvas.drawLine(3 * width/11, 10, width - (width/11), height*9 + 10 , paint);
//        canvas.drawLine(4 * width/11, 10, width - (width/11), height*9 + 10 , paint);
//        canvas.drawLine(5 * width/11, 10, width - (width/11), height*9 + 10 , paint);
//        canvas.drawLine(6 * width/11, 10, width - (width/11), height*9 + 10 , paint);
//        canvas.drawLine(7 * width/11, 10, width - (width/11), height*9 + 10 , paint);
//        canvas.drawLine(8 * width/11, 10, width - (width/11), height*9 + 10 , paint);
//        canvas.drawLine(9 * width/11, 10, width - (width/11), height*9 + 10 , paint);
//        canvas.drawLine(10 * width/11, 10, width - (width/11), height*9 + 10 , paint);
//        view.draw(canvas);

//        drawView = new DrawView(this, width, height);
//        drawView.setBackgroundColor(Color.TRANSPARENT);
//        setContentView(drawView);

    }

    private void checkPermission(){
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        }
    }

    /**
     * Callback for speech recognition activity
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            //recognizeSudoku(photo);
        }
    }

}
