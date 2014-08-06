
package com.bj4.yhh.utilities.weather;

import com.bj4.yhh.utilities.UtilitiesApplication;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

public class WeatherWidgetUpdateService extends Service {
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

    private Runnable mUpdateWidgetRunnable = new Runnable() {

        @Override
        public void run() {
            Context context = WeatherWidgetUpdateService.this;
            AppWidgetManager appWidgetManager = AppWidgetManager
                    .getInstance(getApplicationContext());
            int[] ids = appWidgetManager.getAppWidgetIds(new ComponentName(context
                    .getApplicationContext(), WeatherWidgetOneFour.class));
            for (int id : ids) {
                WeatherWidget.updateWidgets(getApplicationContext(), appWidgetManager, id);
            }
            ids = appWidgetManager.getAppWidgetIds(new ComponentName(context
                    .getApplicationContext(), WeatherWidgetTwoFour.class));
            for (int id : ids) {
                WeatherWidget.updateWidgets(getApplicationContext(), appWidgetManager, id);
            }
            WeatherWidgetUpdateService.this.stopSelf();
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sWorker.removeCallbacks(mUpdateWidgetRunnable);
        sWorker.post(mUpdateWidgetRunnable);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
