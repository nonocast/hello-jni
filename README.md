# Java 通过 JNI 实现跨平台

官方文档 [JNI APIs and Developer Guides](https://docs.oracle.com/javase/8/docs/technotes/guides/jni/)

> Java Native Interface (JNI) is a standard programming interface for writing Java native methods and embedding the Java virtual machine into native applications. The primary goal is binary compatibility of native method libraries across all Java virtual machine implementations on a given platform.

简单来说, 就是让Java程序和Native代码互通的一个机制。

我们通过Hello World来演示如何通过Java调用不同平台(Mac, Linux, Windows和Android)的native code。

我们知道硬件操作的部分都是平台相关的, 所以Java本身没有操作硬件的能力, 我们将操作硬件的场景简化为调用一个native方法后返回一个String。

# Java Side

在Java层面的代码如下:

- 在client下创建一个bootable jar, `gradle init --type java-application`

```java
package cn.nonocast;

public class App {
    public static native String getContent();

    static {
        System.loadLibrary("helloJNI");
    }

    public static void main(String[] args) {
        System.out.println(getContent());
    }
}
```

说明:
- 通过 `System.loadLibrary("helloJNI")` 就实现了在Java中加载native lib, 加载过程需要注意几点:
  - 在 `java.library.path` 中搜索 (参考: [Java中System.loadLibrary() 的执行过程 - WolfCS的个人空间 - OSCHINA](https://my.oschina.net/wolfcs/blog/129696))
  - 搜索的文件名和平台相关
    - Windows: helloJNI.dll
    - linux: libhelloJNI.so
    - Mac OSX: libhelloJNI.jnilib
    - Android: libhelloJNI.so
- getContent 是针对native lib中函数的Java版本声明
- helloJNI为native code, 可以通过任何语言生成, 只要符合操作系统的ELF (Executable and Linking Format), 常规我们都是用C或者C++来实现
    - Windows: Visual Studio 或者 Makefile, compiler: CL.exe
    - linux: gcc
    - Mac OSX: gcc
    - Android: gcc通过NDK做交叉编译
    

注: 是不是随便写一个native lib都可以通过loadLibrary调用? 答案肯定不是的, 只有符合JNI规范的native lib才可以, 那什么是符合JNI规范呢? 
- 满足JNI的函数声明要求, 所以需要`include <jni.h>`
- 满足JNI的参数要求, env, jclass
只有这样才能在实现互操作, 所以JNI更多的是一个协议层面的概念。


当然你现在直接运行这个jar势必报错说找不到native lib

```sh
$ gradle run

> Task :run FAILED
Exception in thread "main" java.lang.UnsatisfiedLinkError: no helloJNI in java.library.path
        at java.lang.ClassLoader.loadLibrary(ClassLoader.java:1867)
        at java.lang.Runtime.loadLibrary0(Runtime.java:870)
        at java.lang.System.loadLibrary(System.java:1122)
        at cn.nonocast.App.<clinit>(App.java:7)

FAILURE: Build failed with an exception.
```

# JNI Protocol Side

针对native的声明我们可以通过javah生成一个header, 即JNI落地到C/C++层面的约束

```sh
javah -jni  -classpath ./build/classes/java/main -d ./jni cn.nonocast.App
```

cn_nonocast_App.h
```h
/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class cn_nonocast_App */

#ifndef _Included_cn_nonocast_App
#define _Included_cn_nonocast_App
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     cn_nonocast_App
 * Method:    getContent
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_cn_nonocast_App_getContent
  (JNIEnv *, jclass);

#ifdef __cplusplus
}
#endif
#endif
```

- jni.h可以在jdk/include中找到, 差不多1600行, 其中定义了jobject, jstring, jint, JNIENV, jclass等。
- 其中jni.h又include了jni_md.h, jni_md.h是一个link, 指向darwin/jni_md.h, linux上则指向linux/jni_md.h, md (machine dependent) 即设备硬件相关。

# Native Side

## Posix

我们将linux和Mac归为一类 Posix, 将cn_nonocast_App.h改名为hello.h复制到./native/Posix下

hello.c

```c
#include "./hello.h"
#include <stdio.h>

JNIEXPORT jstring JNICALL Java_com_shgbit_App_read(JNIEnv *env, jclass cls) {
  return (*env)->NewStringUTF(env, "hello world");
}
```

Makefile
```makefile
# Architecture-dependent library variables
COMPILE := gcc
DELETE := @rm
MKDIR := @mkdir
COPY := @cp
MOVE := @mv
PRINT := @echo
JDK_HOME := $(shell if [ "`uname`" = "Darwin" ]; then echo "`/usr/libexec/java_home`"; else echo "$$JDK_HOME"; fi)
INCLUDES := -I"$(JDK_HOME)/include" -I"$(JDK_HOME)/include/linux" -I"$(JDK_HOME)/include/darwin"
CFLAGS := -fPIC -Os -flto -static-libgcc -U_FORTIFY_SOURCE -D_FORTIFY_SOURCE=0
BUILD_DIR := ../build
LIBRARY_NAME_POSIX := helloJNI.so
LIBRARY_NAME_APPLE := helloJNI.jnilib

# Define phony and suffix rules
.PHONY: all clean linux linux32 linux64 arm armv5 armv6 armv6-hf armv7 armv7-hf armv8_32 armv8_64 solaris solaris32 solaris64 solarisSparc32 solarisSparc64 osx osx64
.SUFFIXES:
.SUFFIXES: .cpp .c .o .class .java .h

all:
	$(PRINT) You must specify either linux or osx!

clean:
	$(DELETE) -rf "$(BUILD_DIR)"

linux: $(BUILD_DIR) $(BUILD)/$(LIBRARY_NAME_POSIX)
osx: $(BUILD_DIR) $(BUILD_DIR)/$(LIBRARY_NAME_APPLE)

$(BUILD_DIR):
	$(MKDIR) -p $@

$(BUILD_DIR)/$(LIBRARY_NAME_POSIX): hello.c
	$(COMPILE) $(INCLUDES) $(CFLAG) -shared $^ -o $@

$(BUILD_DIR)/$(LIBRARY_NAME_APPLE): hello.c
	$(COMPILE) $(INCLUDES) $(CFLAG) -dynamiclib $^ -o $@
```

Makefile就是这么多变量, 其实实质指令就是:
```sh
gcc -I/Library/Java/JavaVirtualMachines/jdk1.8.0_161.jdk/Contents/Home/include -fPIC -shared hello.c -o libhello.jnilib
```

这样分别在linux和osx上执行make linux和make osx就会在../build中生成libhelloJNI.so和libhelloJNI.jnilib。


# 最后

如果在实际项目中，我们一般会将硬件操作的部分放在一个非JNI的native lib中, 比如libhello.so, 这样libhello.so可以供C的client使用, 然后在这个基础上封装libhellJNI.so或者libhelloJNI.jnilib, 同理如果是nodejs使用可以封装为libhelloNodejs.so。

参考项目:
- [Fazecast/jSerialComm: Platform-independent serial port access for Java](https://github.com/Fazecast/jSerialComm)
- [Android-SerialPort-API/SerialPort.c at master · licheedev/Android-SerialPort-API](https://github.com/licheedev/Android-SerialPort-API)
