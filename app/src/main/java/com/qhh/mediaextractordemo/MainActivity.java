package com.qhh.mediaextractordemo;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.File;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private SurfaceView mSurfaceView;
    private String mVedioPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator +"test.mp4";
    private CodecThread mCodecThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSurfaceView = findViewById(R.id.surface_view);

        SurfaceHolder holder = mSurfaceView.getHolder();
        holder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mCodecThread = new CodecThread(holder.getSurface(), mVedioPath);
        mCodecThread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCodecThread.interrupt();
    }
}
