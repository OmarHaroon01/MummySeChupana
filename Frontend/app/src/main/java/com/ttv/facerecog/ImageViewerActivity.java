package com.ttv.facerecog;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.display.DisplayManager;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.view.Display;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

import java.io.File;

public class ImageViewerActivity extends AppCompatActivity implements Application.ActivityLifecycleCallbacks{

    // creating a string variable, image view variable
    // and a variable for our scale gesture detector class.
    String imgPath;
    private ImageView imageView;
    private ScaleGestureDetector scaleGestureDetector;

    private ScreenStateReceiver mReceiver;

    // on below line we are defining our scale factor.
    private float mScaleFactor = 1.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            registerActivityLifecycleCallbacks(this);
            handler = new Handler(getMainLooper());
        }


        // on below line getting data which we have passed from our adapter class.
        imgPath = getIntent().getStringExtra("fileName");

        // initializing our image view.
        imageView = findViewById(R.id.idIVImage);

        // on below line we are initializing our scale gesture detector for zoom in and out for our image.
        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());

        // on below line we are getting our image file from its path.
        File imgFile = new File(imgPath);

        // if the file exists then we are loading that image in our image view.
        if (imgFile.exists()) {
            Picasso.get().load(imgFile).placeholder(R.drawable.ic_baseline_cloud_download_24).into(imageView);
        }

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        mReceiver = new ScreenStateReceiver();
        registerReceiver(mReceiver, intentFilter);


//        startService(new Intent(getApplicationContext(), LockService.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        // inside on touch event method we are calling on
        // touch event method and passing our motion event to it.
        scaleGestureDetector.onTouchEvent(motionEvent);
        return true;
    }

    private Handler handler;
    private Runnable goBack = new Runnable() {
        @Override
        public void run() {
//            Intent intent = new Intent(ImageViewerActivity.this, CameraActivity.class);
//            startActivity(intent);
            finish();
        }
    };


    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
        handler.removeCallbacks(goBack);
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        handler.removeCallbacks(goBack);
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        handler.removeCallbacks(goBack);
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        handler.postDelayed(goBack, 1000);
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {

    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        // on below line we are creating a class for our scale
        // listener and extending it with gesture listener.
        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {

            // inside on scale method we are setting scale
            // for our image in our image view.
            mScaleFactor *= scaleGestureDetector.getScaleFactor();
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 10.0f));

            // on below line we are setting
            // scale x and scale y to our image view.
            imageView.setScaleX(mScaleFactor);
            imageView.setScaleY(mScaleFactor);
            return true;
        }
    }


}
