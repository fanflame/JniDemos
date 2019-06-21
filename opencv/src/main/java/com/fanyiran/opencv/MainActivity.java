package com.fanyiran.opencv;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;

import com.fanyiran.utils.FileUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String MODEL_NAME = "lbpcascade_frontalface.xml";

    static {
        System.loadLibrary("opencv_java3");
        System.loadLibrary("useopencv");
    }

    private static final int REQUEST_CAMERA = 0x01;

    private CameraSurfaceView mCameraSurfaceView;
    private Button mBtnSwitch;
    private int cvWidth;
    private int cvHeight;

//    private int mOrientation;

    // CameraSurfaceView 容器包装类
    private FrameLayout mAspectLayout;
    private boolean mCameraRequested;
    private int cameraId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        initCV();
        setContentView(R.layout.activity_main);
        // Android 6.0相机动态权限检查
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            initView();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    }, REQUEST_CAMERA);
        }
    }

    public void initCV() {
        File cachedFile = getCacheDir();
        File file = new File(cachedFile,MODEL_NAME);
        if (!cachedFile.exists()) {
            try {
                cachedFile.mkdirs();
            } catch (Exception e) {

            }
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {

            }
            FileUtils.assetCopyToSdCard(this,MODEL_NAME,
                    file.getAbsolutePath());
        }
        initFaceDetecter(file.getAbsolutePath());
    }

    /**
     * 初始化View
     */
    private void initView() {
        mAspectLayout = (FrameLayout) findViewById(R.id.layout_aspect);
        mCameraSurfaceView = new CameraSurfaceView(this);
        mCameraSurfaceView.setOnBindHolderListener(new CameraSurfaceView.OnBindHolderListener() {
            @Override
            public void onBindHolder(SurfaceHolder holder, int width, int height) {
                setSurface(holder.getSurface());
                cvWidth = width;
                cvHeight = height;
            }
        });
        cameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
        mCameraSurfaceView.setCallback(callback);
        mAspectLayout.addView(mCameraSurfaceView);
//        mOrientation = CameraUtils.calculateCameraPreviewOrientation(this);
        mBtnSwitch = (Button) findViewById(R.id.btn_switch);
        mBtnSwitch.setOnClickListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            // 相机权限
            case REQUEST_CAMERA:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mCameraRequested = true;
                    initView();
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCameraRequested) {
            CameraUtils.startPreview();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        CameraUtils.stopPreview();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_switch:
                switchCamera();
                break;
        }
    }

    /**
     * 切换相机
     */
    private void switchCamera() {
        if (mCameraSurfaceView != null) {
            cameraId = 1 - CameraUtils.getCameraID();
            CameraUtils.switchCamera(cameraId, mCameraSurfaceView.getHolder(), callback);
            // 切换相机后需要重新计算旋转角度
//            mOrientation = CameraUtils.calculateCameraPreviewOrientation(this);
        }
    }

    private Camera.PreviewCallback callback = new Camera.PreviewCallback() {

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            faceDetected(data, CameraUtils.DEFAULT_WIDTH, CameraUtils.DEFAULT_HEIGHT, cameraId);
        }
    };

    @Override
    protected void onDestroy() {
        release();
        super.onDestroy();
    }

    native void initFaceDetecter(String model);

    native void setSurface(Surface surface);

    native void faceDetected(byte[] data, int w, int h, int cameraId);

    native void release();

}
