//
// Created by MOMO on 2019-06-13.
//

#include <jni.h>
#include <string>
#include <avutil.h>

extern "C"
JNIEXPORT jstring JNICALL
Java_com_fanyiran_ffmpegstatic_MainActivity_getFFmpegVersion(JNIEnv *env, jobject thiz) {
    char * av_version = const_cast<char *>(av_version_info());
    return env->NewStringUTF(av_version);
}




