
package com.bj4.yhh.utilities;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingManager {
    private static final String WEATHER_HAS_INIT = "weather_has_init";

    private Context mContext;

    private static SettingManager sInstance;

    private SharedPreferences mSharedPreferences;

    public synchronized static SettingManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new SettingManager(context);
        }
        return sInstance;
    }

    private SettingManager(Context context) {
        mContext = context.getApplicationContext();
    }

    public boolean hasWeatherDataInit() {
        return getPref().getBoolean(WEATHER_HAS_INIT, false);
    }

    public void setWeatherDataInit() {
        getPref().edit().putBoolean(WEATHER_HAS_INIT, true).apply();
    }

    private synchronized SharedPreferences getPref() {
        if (mSharedPreferences == null) {
            mSharedPreferences = mContext.getSharedPreferences("settingmanager",
                    Context.MODE_PRIVATE);
        }
        return mSharedPreferences;
    }
}
