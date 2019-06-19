package com.fanyiran.opencv;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Surface;

public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("useropencv");
        System.loadLibrary("libopencv_java3");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // faceDetected(data,width,height,mCameraId);
    }

    native void initFaceDetecter(String model);

    native void setSurface(Surface surface);

    native void faceDetected(byte[] data,int w,int h,int cameraId);

}
