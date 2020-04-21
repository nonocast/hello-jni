# Java 通过 JNI 实现跨平台

官方文档 [JNI APIs and Developer Guides](https://docs.oracle.com/javase/8/docs/technotes/guides/jni/)

> Java Native Interface (JNI) is a standard programming interface for writing Java native methods and embedding the Java virtual machine into native applications. The primary goal is binary compatibility of native method libraries across all Java virtual machine implementations on a given platform.

简单来说, 就是让Java程序和Native代码互通的一个机制。

我们通过Hello World来演示如何通过Java调用不同平台(Mac, Linux, Windows和Android)的native code。

我们知道硬件操作的部分都是平台相关的, 所以Java本身没有操作硬件的能力, 我们将操作硬件的场景简化为调用一个native方法后返回一个String。

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
  - 在 `java.library.path` 中搜索
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
    






参考项目:
- [Fazecast/jSerialComm: Platform-independent serial port access for Java](https://github.com/Fazecast/jSerialComm)
- [Android-SerialPort-API/SerialPort.c at master · licheedev/Android-SerialPort-API](https://github.com/licheedev/Android-SerialPort-API)
