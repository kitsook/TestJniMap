#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "net_clarenceho_jnimap_JniMap.h"

JNIEXPORT jobject JNICALL Java_net_clarenceho_jnimap_JniMap_process(JNIEnv* env, jobject thisObject, jint map_len,
    jboolean freeTmpLocalRef, jboolean freeReturnedLocalRef, jboolean isLeaky) {

    // prepare Java HashMap calls
    jclass mapClass = (*env)->FindClass(env, "java/util/HashMap");
    if (mapClass == NULL) return NULL;
    jmethodID mapInit = (*env)->GetMethodID(env, mapClass, "<init>", "(I)V");
    if (mapInit == NULL) {
        (*env)->DeleteLocalRef(env, mapClass);
        return NULL;
    }
    jmethodID hashMapPut = (*env)->GetMethodID(env, mapClass, "put",
                "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
    if (hashMapPut == NULL) {
        (*env)->DeleteLocalRef(env, mapClass);
        return NULL;
    }

    // prepare Java Integer calls
    jclass integerClass = (*env)->FindClass(env, "java/lang/Integer");
    if (NULL == integerClass) {
        (*env)->DeleteLocalRef(env, mapClass);
        return NULL;
    }
    jmethodID integerInit = (*env)->GetMethodID(env, integerClass, "<init>", "(I)V");
    if (NULL == integerInit) {
        (*env)->DeleteLocalRef(env, integerClass);
        (*env)->DeleteLocalRef(env, mapClass);
        return NULL;
    }

    jobject hashMap = (*env)->NewObject(env, mapClass, mapInit, map_len);

    char *buf = (char*)malloc(12);
    strcpy(buf, "hello world");

    for (int i = 0; i < map_len; i++) {
        jobject key = (*env)->NewObject(env, integerClass, integerInit, i);
        jstring payload = (*env)->NewStringUTF(env, buf);
        (*env)->CallObjectMethod(env, hashMap, hashMapPut, key, payload);

        if (freeTmpLocalRef) {
            (*env)->DeleteLocalRef(env, key);
            (*env)->DeleteLocalRef(env, payload);
        }
    }

    if (isLeaky) {
        // !! simulate memory leak with global references !!
        (*env)->NewGlobalRef(env, hashMap);
    }

    free(buf);
    if (freeTmpLocalRef) {
        (*env)->DeleteLocalRef(env, integerClass);
        (*env)->DeleteLocalRef(env, mapClass);
    }
    if (freeReturnedLocalRef) {
        // !! this will cause null value returned !!
        (*env)->DeleteLocalRef(env, hashMap);
    }

    return hashMap;
}
