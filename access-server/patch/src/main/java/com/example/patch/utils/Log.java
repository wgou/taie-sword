package com.example.patch.utils;

public class Log {
    public static void i(String msg, Object... args) {
        System.out.println(String.format(msg, args));
    }

    public static void e(String msg, Object... args) {
        String _msg = String.format(msg, args);
        System.err.println(_msg);
    }

    public static void e(String msg, Throwable ex, Object... args) {
        e(msg, args);
        ex.printStackTrace();
    }

}
