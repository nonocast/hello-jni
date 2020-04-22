#include "./hello.h"
#include <stdio.h>

JNIEXPORT jstring JNICALL Java_cn_nonocast_App_getContent(JNIEnv *env,
                                                          jclass cls) {
  return (*env)->NewStringUTF(env, "hello world");
}