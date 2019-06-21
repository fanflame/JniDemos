package com.fanyiran.opencv;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = CameraSurfaceView.class.getSimpleName();
    private OnBindHolderListener onBindHolderListener;
    private SurfaceHolder mSurfaceHolder;
    private Camera.PreviewCallback callback;
    private boolean isSufacechanged;
    SurfaceHolder holder;

    public CameraSurfaceView(Context context) {
        super(context);
        init();
    }

    public CameraSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CameraSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        CameraUtils.openFrontalCamera(CameraUtils.DESIRED_PREVIEW_FPS);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (!isSufacechanged) {
            isSufacechanged = true;
            this.holder = holder;
        }
        CameraUtils.startPreviewDisplay(holder,callback);
        if (onBindHolderListener != null) {
            onBindHolderListener.onBindHolder(holder,width,height);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        CameraUtils.releaseCamera();
    }

    public void setOnBindHolderListener(OnBindHolderListener onBindHolderListener) {
        this.onBindHolderListener = onBindHolderListener;
    }

    public void setCallback(Camera.PreviewCallback callback) {
        this.callback = callback;
        if (isSufacechanged) {
            CameraUtils.startPreviewDisplay(holder,callback);
        }
    }

    public interface OnBindHolderListener {
        void onBindHolder(SurfaceHolder holder,int width,int height);
    }

}
