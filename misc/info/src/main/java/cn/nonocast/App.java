package cn.nonocast;

public class App {
    public void run() {
        System.out.println("os.name: " + System.getProperty("os.name"));
        System.out.println("os.version: " + System.getProperty("os.version"));
        System.out.println("os.arch: " + System.getProperty("os.arch"));
        System.out.println("os.arch_full: " + System.getProperty("os.arch_full"));
    }

    public static void main(String[] args) {
        new App().run();
    }
}
