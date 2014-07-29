
package com.bj4.yhh.utilities.music;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;

public class MusicPlayService extends Service {
    private static final HandlerThread sWorkerThread = new HandlerThread("PlayMusicService-player");
    static {
        sWorkerThread.start();
        sWorkerThread.setPriority(Thread.MAX_PRIORITY);
    }

    private static final Handler sWorker = new Handler(sWorkerThread.getLooper());

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
