#include <jni.h>
#include <string>

extern "C"
JNIEXPORT jstring JNICALL
Java_gr_hua_www_v2of2_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
