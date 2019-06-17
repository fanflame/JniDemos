//
// Created by fanyiran on 2019-06-13.
//

#include <jni.h>
#include <string>

extern "C"{
#include <avutil.h>

JNIEXPORT jstring JNICALL
Java_com_fanyiran_ffmpegshare_MainActivity_getVersion(JNIEnv *env, jobject thiz) {
    char * av_version = const_cast<char *>(av_version_info());
    return env->NewStringUTF(av_version);
}
}



