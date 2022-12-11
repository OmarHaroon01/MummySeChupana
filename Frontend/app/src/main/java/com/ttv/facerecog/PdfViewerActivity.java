package com.ttv.facerecog;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.danjdt.pdfviewer.PdfViewer;
import com.danjdt.pdfviewer.interfaces.OnPageChangedListener;
import com.danjdt.pdfviewer.utils.PdfPageQuality;

public class PdfViewerActivity extends AppCompatActivity implements OnPageChangedListener, Application.ActivityLifecycleCallbacks {
    Integer pageNumber = 0;
    String file_name;
    private ScreenStateReceiver mReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_viewer);

        FrameLayout fl = findViewById(R.id.rootView);

        Intent intent = getIntent();
        file_name = intent.getStringExtra("fileName");


        java.io.File file = new java.io.File(file_name);
        new PdfViewer.Builder(fl)
                .setZoomEnabled(true)
                .quality(PdfPageQuality.QUALITY_1080)
                .setMaxZoom(3f)
                .setOnPageChangedListener(this)
                .build()
                .load(file);


        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        mReceiver = new ScreenStateReceiver();
        registerReceiver(mReceiver, intentFilter);


    }



    @Override
    public void onPageChanged(int i, int i1) {
        TextView tv = findViewById(R.id.tvCounter);
        tv.setText(getString(R.string.pdf_page_counter, i, i1));
    }

    private Handler handler;
    private Runnable goBack = new Runnable() {
        @Override
        public void run() {
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

}