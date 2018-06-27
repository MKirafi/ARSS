/*
 * Source: https://medium.com/@ssaurel/create-a-splash-screen-on-android-the-right-way-93d6fb444857
 */
package com.example.uva.arss;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import static java.lang.Thread.sleep;


public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splashscreen);

        ImageView imgView=(ImageView) findViewById(R.id.Logo);
        imgView.setScaleType(ImageView.ScaleType.CENTER);
        Drawable drawable  = getResources().getDrawable(R.mipmap.ic_launcher);
        imgView.setImageDrawable(drawable);

        System.out.println("HALLO");
        try {
            sleep(5000);
        } catch(Exception e) {
            System.out.println("ja");
        }

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
