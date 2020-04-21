#include "./hello.h"
#include <stdio.h>

JNIEXPORT jstring JNICALL Java_com_shgbit_App_read(JNIEnv *env, jclass cls) {
  return (*env)->NewStringUTF(env, "hello world");
}