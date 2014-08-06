
package com.bj4.yhh.utilities;

import java.util.HashMap;

import com.bj4.yhh.utilities.analytics.Analytics;
import com.bj4.yhh.utilities.analytics.flurry.FlurryTracker;
import com.bj4.yhh.utilities.analytics.mixpanel.MixpanelTracker;
import com.bj4.yhh.utilities.util.Utils;
import com.bj4.yhh.utilities.weather.Weather;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class SettingManager {
    private static final String WEATHER_HAS_INIT = "weather_has_init";

    private static final String WEATHER_USING_SIMPLE_VIEW = "weather_simple_view";

    private static final String SETTINGS_ENABLE_GA = "enable_ga";

    private static final String WEATHER_USING_C = "using_c";

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

    public boolean isUsingC() {
        return getPref().getBoolean(WEATHER_USING_C, false);
    }

    public void setUsingC(final boolean usingC) {
        if (usingC != isUsingC()) {
            getPref().edit().putBoolean(WEATHER_USING_C, usingC).commit();
            Utils.forcedReloadWeatherDataCache(mContext);
            MixpanelTracker.getTracker(mContext).track(Analytics.TemptureUnit.EVENT,
                    usingC ? Analytics.TemptureUnit.CELCIUS : Analytics.TemptureUnit.FAHRENHEIT,
                    null);
            HashMap<String, String> flurryTrackMap = new HashMap<String, String>();
            flurryTrackMap.put(usingC ? Analytics.TemptureUnit.CELCIUS
                    : Analytics.TemptureUnit.FAHRENHEIT, null);
            FlurryTracker.getInstance().track(Analytics.TemptureUnit.EVENT, flurryTrackMap);
        }
    }

    public boolean isEnableGa() {
        return getPref().getBoolean(SETTINGS_ENABLE_GA, true);
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
