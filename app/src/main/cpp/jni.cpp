#include <jni.h>
#include <string>
#include <android/log.h>

using namespace std;

extern "C" {

    void log_error(const char *err) {
        __android_log_print(ANDROID_LOG_ERROR, "JNI", "%s", err);
    }

    jclass get_helper(JNIEnv *env) {
        //Get helper class
        jclass helperClass = env->FindClass("by/ssrlab/qrscanner/Helper");
        if (!helperClass) {
            log_error("Helper class not found");
            return nullptr;
        }

        return helperClass;
    }

    jobject get_helper_obj(JNIEnv *env, jclass helperClass) {
        //Get helper <init> method
        jmethodID helperConstructor = env->GetMethodID(helperClass, "<init>", "()V");
        if (!helperConstructor) {
            log_error("Cannot find helper constructor");
            return nullptr;
        }

        //Get helper object
        jobject helperObj = env->NewObject(helperClass, helperConstructor);
        if (!helperObj) {
            log_error("Cannot initialize helper object");
            return nullptr;
        }

        return helperObj;
    }

    JNIEXPORT void JNICALL
    Java_by_ssrlab_qrscanner_MainActivity_requestPermissionJNI(JNIEnv *env, jobject thiz) {

        //Get helper method request permission
        jmethodID requestPermissionMethod = env->GetMethodID(get_helper(env), "requestPermission", "(Lby/ssrlab/qrscanner/MainActivity;)V");
        if (!requestPermissionMethod) {
            log_error("requestPermission method not found");
            return;
        }

        env->CallVoidMethod(get_helper_obj(env, get_helper(env)), requestPermissionMethod, thiz);
    }

    JNIEXPORT void JNICALL
    Java_by_ssrlab_qrscanner_MainActivity_initJNI(JNIEnv *env, jobject thiz) {
        jmethodID init = env->GetMethodID(get_helper(env), "init", "(Lby/ssrlab/qrscanner/MainActivity;)V");
        if (!init) {
            log_error("Cannot find init method");
            return;
        }

        env->CallVoidMethod(get_helper_obj(env, get_helper(env)), init, thiz);
    }
}