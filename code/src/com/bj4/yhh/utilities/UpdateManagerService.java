
package com.bj4.yhh.utilities;

import com.bj4.yhh.utilities.weather.WeatherService;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class UpdateManagerService extends Service {
    private static final String TAG = "UpdateManagerService";

    public static final String UPDATE_TYPE = "update_type";

    public static final int UPDATE_NONE = -2;

    public static final int UPDATE_ALL = -1;

    public static final int UPDATE_TYPE_WEATHER = 0;

    public static final int UPDATE_TYPE_MUSIC = 1;

    private int mUpdateWeatherInterval = 3 * 1000 * 60;

    private Handler mHandler = new Handler();

    @Override
    public void onCreate() {
        super.onCreate();
        scheduleUpdate(UPDATE_ALL);
    }

    private void scheduleUpdate(final int type) {
        switch (type) {
            case UPDATE_TYPE_WEATHER:
                mHandler.post(mUpdateWeatherHandler);
                break;
            case UPDATE_TYPE_MUSIC:
                break;
            case UPDATE_ALL:
                mHandler.post(mUpdateWeatherHandler);
                break;
            case UPDATE_NONE:
                return;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                final int type = extras.getInt(UPDATE_TYPE);
                scheduleUpdate(type);
            }
        }
        return Service.START_STICKY;
    }

    private Runnable mUpdateWeatherHandler = new Runnable() {

        @Override
        public void run() {
            Log.d(TAG, "UpdateWeather");
            Intent intent = new Intent(UpdateManagerService.this, WeatherService.class);
            intent.putExtra(WeatherService.INTENT_UPDATE_ALL, true);
            UpdateManagerService.this.startService(intent);
            mHandler.removeCallbacks(mUpdateWeatherHandler);
            mHandler.postDelayed(mUpdateWeatherHandler, mUpdateWeatherInterval);
        }
    };

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

}
