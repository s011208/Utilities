
package com.bj4.yhh.utilities.floatingwindow;

import java.util.ArrayList;

import com.bj4.yhh.utilities.R;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class FloatingWindowService extends Service implements FloatingWindow.FloatingWindowCallback {
    public static final String TAG = "QQQQ";

    public static final boolean DEBUG = true;

    public static final String INTENT_START_WINDOW = "intent_start_window";

    public static final int INTENT_WINDOW_TYPE_WEATHER = 0;
    public static final int INTENT_WINDOW_TYPE_CALCULATOR = 1;

    private ArrayList<String> mWindowList = new ArrayList<String>();

    @Override
    public void onCreate() {
        super.onCreate();
        startForeground(FloatingWindowService.class.hashCode(), createNotification(this));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                int windowType = extras.getInt(INTENT_START_WINDOW, -1);
                String key = null;
                switch (windowType) {
                    case INTENT_WINDOW_TYPE_WEATHER:
                        key = WeatherFloatingWindow.class.toString();
                        if (mWindowList.contains(key) == false) {
                            mWindowList.add(key);
                            if (DEBUG)
                                Log.d(TAG, "add weather");
                            new WeatherFloatingWindow(this).setCallback(this);
                        }
                        break;
                    case INTENT_WINDOW_TYPE_CALCULATOR:
                        key = CalculatorFloatingWindow.class.toString();
                        if (mWindowList.contains(key) == false) {
                            mWindowList.add(key);
                            if (DEBUG)
                                Log.d(TAG, "add calculator");
                            new CalculatorFloatingWindow(this).setCallback(this);
                        }
                        break;
                }
            }
        }
        return Service.START_STICKY;
    }

    private static Notification createNotification(Context context) {
        Notification noti = new Notification.Builder(context).setContentTitle("Floating Shortcut")
                .setContentText("Floating Shortcut Manager").setSmallIcon(R.drawable.ic_launcher)
                .build();
        return noti;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCloseWindow(String classKey) {
        if (classKey == null)
            return;
        mWindowList.remove(classKey);
        if(mWindowList.isEmpty()){
            stopSelf();
        }
    }

}
