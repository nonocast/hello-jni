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
