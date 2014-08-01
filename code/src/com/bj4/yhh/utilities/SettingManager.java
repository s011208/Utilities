
package com.bj4.yhh.utilities;

import com.bj4.yhh.utilities.analytics.Analytics;
import com.bj4.yhh.utilities.weather.Weather;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class SettingManager {
    private static final String WEATHER_HAS_INIT = "weather_has_init";

    private static final String WEATHER_USING_SIMPLE_VIEW = "weather_simple_view";

    private static final String SETTINGS_ENABLE_GA = "enable_ga";

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

    public boolean isEnableGa() {
        return getPref().getBoolean(SETTINGS_ENABLE_GA, false);
    }

    public void setEnableGa(final boolean enableGa) {
        getPref().edit().putBoolean(SETTINGS_ENABLE_GA, enableGa).apply();
        Analytics.ENABLE_TRACKER = enableGa;
    }

    public boolean isWeatherUsingSimpleView() {
        return getPref().getBoolean(WEATHER_USING_SIMPLE_VIEW, false);
    }

    public void setWeatherUsingSimpleView(boolean simple) {
        getPref().edit().putBoolean(WEATHER_USING_SIMPLE_VIEW, simple).apply();
        mContext.sendBroadcast(new Intent(Weather.INTENT_ON_DATA_UPDATE));
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
