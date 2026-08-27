package com.herbillon.guitar.utils;

import android.os.Handler;
import android.os.Looper;
import com.herbillon.guitar.network.GuitarAPI;

public class KeepAliveScheduler {
    private static final long PING_INTERVAL_MS = 13 * 60 * 1000L; // 13 min

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final GuitarAPI guitarAPI;
    private final Runnable pingTask;
    private boolean isRunning = false;

    public KeepAliveScheduler(GuitarAPI guitarAPI) {
        this.guitarAPI = guitarAPI;
        this.pingTask = new Runnable() {
            @Override
            public void run() {
                guitarAPI.pingApi();
                handler.postDelayed(this, PING_INTERVAL_MS);
            }
        };
    }

    public void start() {
        if (isRunning) return;
        isRunning = true;
        handler.postDelayed(pingTask, PING_INTERVAL_MS);
    }

    public void stop() {
        isRunning = false;
        handler.removeCallbacks(pingTask);
    }
}
