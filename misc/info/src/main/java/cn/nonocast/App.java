package cn.nonocast;

public class App {
    public void run() {
        System.out.println(System.getProperty("os.name"));
        System.out.println(System.getProperty("os.version"));
        System.out.println(System.getProperty("os.arch"));
        System.out.println(System.getProperty("os.arch_full"));
    }

    public static void main(String[] args) {
        new App().run();
    }
}
