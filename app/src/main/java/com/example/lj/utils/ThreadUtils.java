package com.example.lj.utils;

import android.os.Handler;

public class ThreadUtils {
    // 启动普通线程
    public static void runInThread(Runnable task) {
        new Thread(task).start();
    }

    // 启动UI子线程
    public static Handler handler = new Handler();
    public static void runInUIThread(Runnable task) {
        handler.post(task);
    }
}