package com.fanyiran.opencv;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.view.Surface;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CameraUtils {

    // 相机默认宽高，相机的宽度和高度跟屏幕坐标不一样，手机屏幕的宽度和高度是反过来的。
    public static final int DEFAULT_WIDTH = 1280;
    public static final int DEFAULT_HEIGHT = 720;
    public static final int DESIRED_PREVIEW_FPS = 30;

    private static int mCameraID = Camera.CameraInfo.CAMERA_FACING_FRONT;
    private static Camera mCamera;
    private static int mCameraPreviewFps;
    private static byte[] buffer;
//    private static int mOrientation = 0;
//    private static Camera.PreviewCallback sCallback;
    /**
     * 打开相机，默认打开前置相机
     * @param expectFps
     */
    public static void openFrontalCamera(int expectFps) {
        if (mCamera != null) {
            throw new RuntimeException("camera already initialized!");
        }
        Camera.CameraInfo info = new Camera.CameraInfo();
        int numCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numCameras; i++) {
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                mCamera = Camera.open(i);
                mCameraID = info.facing;
                break;
            }
        }
        // 如果没有前置摄像头，则打开默认的后置摄像头
        if (mCamera == null) {
            mCamera = Camera.open();
            mCameraID = Camera.CameraInfo.CAMERA_FACING_BACK;
        }
        // 没有摄像头时，抛出异常
        if (mCamera == null) {
            throw new RuntimeException("Unable to open camera");
        }

        Camera.Parameters parameters = mCamera.getParameters();
        mCameraPreviewFps = CameraUtils.chooseFixedPreviewFps(parameters, expectFps * 1000);
        parameters.setRecordingHint(true);
        mCamera.setParameters(parameters);
        setPreviewSize(mCamera, CameraUtils.DEFAULT_WIDTH, CameraUtils.DEFAULT_HEIGHT);
        setPictureSize(mCamera, CameraUtils.DEFAULT_WIDTH, CameraUtils.DEFAULT_HEIGHT);
//        mCamera.setDisplayOrientation(mOrientation);
    }

    /**
     * 根据ID打开相机
     * @param cameraID
     * @param expectFps
     */
    public static void openCamera(int cameraID, int expectFps) {
        if (mCamera != null) {
            throw new RuntimeException("camera already initialized!");
        }
        mCamera = Camera.open(cameraID);
        if (mCamera == null) {
            throw new RuntimeException("Unable to open camera");
        }
        mCameraID = cameraID;
        Camera.Parameters parameters = mCamera.getParameters();
        mCameraPreviewFps = CameraUtils.chooseFixedPreviewFps(parameters, expectFps * 1000);
        parameters.setRecordingHint(true);
        parameters.setPreviewFormat(ImageFormat.NV21);
        mCamera.setParameters(parameters);
        setPreviewSize(mCamera, CameraUtils.DEFAULT_WIDTH, CameraUtils.DEFAULT_HEIGHT);
        setPictureSize(mCamera, CameraUtils.DEFAULT_WIDTH, CameraUtils.DEFAULT_HEIGHT);
//        mCamera.setDisplayOrientation(mOrientation);
    }

    /**
     * 开始预览
     * @param holder
     */
    public static void startPreviewDisplay(SurfaceHolder holder,final Camera.PreviewCallback callback) {
        if (mCamera == null) {
            throw new IllegalStateException("Camera must be set when start preview");
        }
        try {
            // TODO: 2019-06-21 这么设置居然在camera预览里有效果了，why？
            SurfaceTexture surfaceTexture = new SurfaceTexture(11);
            mCamera.setPreviewTexture(surfaceTexture);
//            mCamera.setPreviewDisplay(holder);
            buffer = new byte[DEFAULT_WIDTH * DEFAULT_HEIGHT * 3/2];
            mCamera.addCallbackBuffer(buffer);
            mCamera.setPreviewCallbackWithBuffer(new Camera.PreviewCallback(){

                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    callback.onPreviewFrame(data,camera);
                    mCamera.addCallbackBuffer(buffer);
                }
            });
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 切换相机
     * @param cameraID
     */
    public static void switchCamera(int cameraID, SurfaceHolder holder,Camera.PreviewCallback callback) {
        if (mCameraID == cameraID) {
            return;
        }
        mCameraID = cameraID;
        // 释放原来的相机
        releaseCamera();
        // 打开相机
        openCamera(cameraID, CameraUtils.DESIRED_PREVIEW_FPS);
        // 打开预览
        startPreviewDisplay(holder,callback);
    }

    /**
     * 释放相机
     */
    public static void releaseCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * 开始预览
     */
    public static void startPreview() {
        if (mCamera != null) {
            mCamera.startPreview();
        }
    }

    /**
     * 停止预览
     */
    public static void stopPreview() {
        if (mCamera != null) {
            mCamera.stopPreview();
        }
    }

    /**
     * 拍照
     */
    public static void takePicture(Camera.ShutterCallback shutterCallback,
                                   Camera.PictureCallback rawCallback,
                                   Camera.PictureCallback pictureCallback) {
        if (mCamera != null) {
            mCamera.takePicture(shutterCallback, rawCallback, pictureCallback);
        }
    }

    /**
     * 设置预览大小
     * @param camera
     * @param expectWidth
     * @param expectHeight
     */
    public static void setPreviewSize(Camera camera, int expectWidth, int expectHeight) {
        Camera.Parameters parameters = camera.getParameters();
//        Camera.Size size = calculatePerfectSize(parameters.getSupportedPreviewSizes(),
//                expectWidth, expectHeight);
        parameters.setPreviewSize(expectWidth, expectHeight);
        camera.setParameters(parameters);
    }

    /**
     * 获取预览大小
     * @return
     */
    public static Camera.Size getPreviewSize() {
        if (mCamera != null) {
            return mCamera.getParameters().getPreviewSize();
        }
        return null;
    }

    /**
     * 设置拍摄的照片大小
     * @param camera
     * @param expectWidth
     * @param expectHeight
     */
    public static void setPictureSize(Camera camera, int expectWidth, int expectHeight) {
        Camera.Parameters parameters = camera.getParameters();
        Camera.Size size = calculatePerfectSize(parameters.getSupportedPictureSizes(),
                expectWidth, expectHeight);
        parameters.setPictureSize(size.width, size.height);
        camera.setParameters(parameters);
    }

    /**
     * 获取照片大小
     * @return
     */
    public static Camera.Size getPictureSize() {
        if (mCamera != null) {
            return mCamera.getParameters().getPictureSize();
        }
        return null;
    }

    /**
     * 计算最完美的Size
     * @param sizes
     * @param expectWidth
     * @param expectHeight
     * @return
     */
    public static Camera.Size calculatePerfectSize(List<Camera.Size> sizes, int expectWidth,
                                                   int expectHeight) {
        sortList(sizes); // 根据宽度进行排序
        Camera.Size result = sizes.get(0);
        boolean widthOrHeight = false; // 判断存在宽或高相等的Size
        // 辗转计算宽高最接近的值
        for (Camera.Size size: sizes) {
            // 如果宽高相等，则直接返回
            if (size.width == expectWidth && size.height == expectHeight) {
                result = size;
                break;
            }
            // 仅仅是宽度相等，计算高度最接近的size
            if (size.width == expectWidth) {
                widthOrHeight = true;
                if (Math.abs(result.height - expectHeight)
                        > Math.abs(size.height - expectHeight)) {
                    result = size;
                }
            }
            // 高度相等，则计算宽度最接近的Size
            else if (size.height == expectHeight) {
                widthOrHeight = true;
                if (Math.abs(result.width - expectWidth)
                        > Math.abs(size.width - expectWidth)) {
                    result = size;
                }
            }
            // 如果之前的查找不存在宽或高相等的情况，则计算宽度和高度都最接近的期望值的Size
            else if (!widthOrHeight) {
                if (Math.abs(result.width - expectWidth)
                        > Math.abs(size.width - expectWidth)
                        && Math.abs(result.height - expectHeight)
                        > Math.abs(size.height - expectHeight)) {
                    result = size;
                }
            }
        }
        return result;
    }

    /**
     * 排序
     * @param list
     */
    private static void sortList(List<Camera.Size> list) {
        Collections.sort(list, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size pre, Camera.Size after) {
                if (pre.width > after.width) {
                    return 1;
                } else if (pre.width < after.width) {
                    return -1;
                }
                return 0;
            }
        });
    }

    /**
     * 选择合适的FPS
     * @param parameters
     * @param expectedThoudandFps 期望的FPS
     * @return
     */
    public static int chooseFixedPreviewFps(Camera.Parameters parameters, int expectedThoudandFps) {
        List<int[]> supportedFps = parameters.getSupportedPreviewFpsRange();
        for (int[] entry : supportedFps) {
            if (entry[0] == entry[1] && entry[0] == expectedThoudandFps) {
                parameters.setPreviewFpsRange(entry[0], entry[1]);
                return entry[0];
            }
        }
        int[] temp = new int[2];
        int guess;
        parameters.getPreviewFpsRange(temp);
        if (temp[0] == temp[1]) {
            guess = temp[0];
        } else {
            guess = temp[1] / 2;
        }
        return guess;
    }


    /**
     * 获取当前的Camera ID
     * @return
     */
    public static int getCameraID() {
        return mCameraID;
    }

    /**
     * 获取FPS（千秒值）
     * @return
     */
    public static int getCameraPreviewThousandFps() {
        return mCameraPreviewFps;
    }
}