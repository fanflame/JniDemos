//
// Created by fanyiran on 2019-06-13.
//

#include <jni.h>
#include <string>
#include <statictest.h>

extern "C"
JNIEXPORT jint JNICALL
Java_com_fanyiran_usestatic_MainActivity_getIntFromNative(JNIEnv *env, jobject thiz) {
    return staticLibAdd(200,100);
}





