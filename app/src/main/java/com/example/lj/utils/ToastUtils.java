package com.example.lj.utils;

import android.content.Context;
import android.widget.Toast;

public class ToastUtils {
    public static void showToastSafe(final Context context, final String str) {
        ThreadUtils.runInUIThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, str, Toast.LENGTH_LONG).show();
            }
        });
    }
}