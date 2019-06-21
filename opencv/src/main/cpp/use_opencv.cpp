//
// Created by fanyiran on 2019-06-13.
//

#include <jni.h>
#include <string>
#include <android/native_window_jni.h>
#include <opencv2/opencv.hpp>
#include <opencv2/objdetect.hpp>
#include <android/log.h>
using namespace cv;
using namespace std;

#define TAG    "opencv-jni" // 这个是自定义的LOG的标识
#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,TAG,__VA_ARGS__) // 定义LOGD类型

class CascadeDetectorAdapter: public DetectionBasedTracker::IDetector
{
public:
    CascadeDetectorAdapter(Ptr<CascadeClassifier> detector):
            IDetector(),
            Detector(detector)
    {
//        CV_Assert(detector);
    }

    void detect(const Mat &Image, vector<Rect> &objects)
    {
        Detector->detectMultiScale(Image, objects, scaleFactor, minNeighbours, 0, minObjSize, maxObjSize);
    }

    virtual ~CascadeDetectorAdapter()
    {
    }

private:
    CascadeDetectorAdapter();
    Ptr<CascadeClassifier> Detector;
};

extern "C" {
CascadeClassifier *pClassifier = 0;
ANativeWindow *window = 0;
DetectionBasedTracker *tracker = 0;

JNIEXPORT void JNICALL
Java_com_fanyiran_opencv_MainActivity_initFaceDetecter(JNIEnv *env, jobject thiz,
                                                          jstring model_) {
    const char *path = env->GetStringUTFChars(model_, NULL);
    LOGD("model_:%s",path);
    // 第一种方案
//    if (pClassifier == nullptr) {
//        pClassifier = new CascadeClassifier(path);
//    }

//    // 第二种方案
    //创建一个跟踪适配器
    Ptr<CascadeDetectorAdapter> mainDetector = makePtr<CascadeDetectorAdapter>(makePtr<CascadeClassifier>(path));
    //创建一个跟踪适配器
    Ptr<CascadeDetectorAdapter> trackingDetector  = makePtr<CascadeDetectorAdapter>(makePtr<CascadeClassifier>(path));
    DetectionBasedTracker::Parameters parameters;
    //创建一个跟踪器
    tracker = new DetectionBasedTracker(mainDetector,trackingDetector,parameters);
    //开始跟踪
    tracker->run();

    env->ReleaseStringUTFChars(model_, path);

}
JNIEXPORT void JNICALL
Java_com_fanyiran_opencv_MainActivity_setSurface(JNIEnv *env, jobject thiz, jobject surface) {
    if (window) {
        ANativeWindow_release(window);
        window = 0;
    }
    window = ANativeWindow_fromSurface(env, surface);
}

JNIEXPORT void JNICALL
Java_com_fanyiran_opencv_MainActivity_faceDetected(JNIEnv *env, jobject thiz, jbyteArray data_,
                                                      jint width, jint height, jint cameraId) {
    jbyte *data = env->GetByteArrayElements(data_, NULL);
    //将data->Mat

    Mat src(height + height / 2, width, CV_8UC1, data);
    cvtColor(src, src, COLOR_YUV2RGBA_NV21);
    if (cameraId == 1) {
        // 前置，逆时针90
        rotate(src,src,ROTATE_90_COUNTERCLOCKWISE);
        // 水平方向翻转
        flip(src,src,1);
    } else{
        // 后置摄像头
        rotate(src,src,ROTATE_90_CLOCKWISE);
    }
    Mat grayMat;
    // 灰度化处理，排除彩色信息干扰，黑白图片
    cvtColor(src,grayMat,COLOR_RGBA2GRAY);
    // 均衡化处理，增强图像对比度
    equalizeHist(grayMat,grayMat);

    // 容器
    vector<Rect> faces;
    //第一种识别
//    pClassifier->detectMultiScale(grayMat,faces);

    //第二种识别
    tracker->process(grayMat);
    tracker->getObjects(faces);

    for (int i = 0; i < faces.size(); ++i) {
        Rect face = faces[i];
        rectangle(src,face,Scalar(255,0,0));

    }

    if (window) {
        // 设置缓冲区形状
        ANativeWindow_setBuffersGeometry(window, src.cols, src.rows, WINDOW_FORMAT_RGBA_8888);
        ANativeWindow_Buffer buffer;
        do {
            if (ANativeWindow_lock(window, &buffer, 0)) {
                ANativeWindow_release(window);
                window = 0;
                break;
            } else {
                //src.data -> buffer.bits
                // 填充rgb  数据给懂dst_data
                uint8_t *dst_data = static_cast<uint8_t *>(buffer.bits);
                // 一行有多少个数据
                int dst_line_size = buffer.stride * 4;
                for (int i = 0; i < buffer.height; ++i) {
                    memcpy(dst_data + i * dst_line_size, src.data + i * src.cols * 4,
                           dst_line_size);
                }
                // 提交刷新
                ANativeWindow_unlockAndPost(window);
            }
        } while (0);
    } else {
        LOGD("window is null");
    }

    src.release();
    grayMat.release();

    env->ReleaseByteArrayElements(data_,data,0);
}

JNIEXPORT void JNICALL
Java_com_fanyiran_opencv_MainActivity_release(JNIEnv *env, jobject thiz) {
    if (tracker) {
        tracker->stop();
        delete tracker;
        tracker = 0;
    }
}
}





