
package com.bj4.yhh.utilities.weather;

import java.util.HashMap;

import com.bj4.yhh.utilities.R;
import com.bj4.yhh.utilities.UtilitiesApplication;
import com.bj4.yhh.utilities.analytics.Analytics;
import com.bj4.yhh.utilities.analytics.flurry.FlurryTracker;
import com.bj4.yhh.utilities.analytics.mixpanel.MixpanelTracker;
import com.bj4.yhh.utilities.util.Utils;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.RemoteViews;

public class WeatherWidget extends AppWidgetProvider {
    private static final String TAG = "QQQQ";

    private static final int WIDGET_UNIT = 72;

    private static SharedPreferences sPref;

    private static synchronized SharedPreferences getPref(Context context) {
        if (sPref == null) {
            sPref = context.getApplicationContext().getSharedPreferences(
                    WeatherWidgetConfiguration.WIDGET_PROVIDER_DATA, Context.MODE_PRIVATE);
        }
        return sPref;
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        for (int widgetId : appWidgetIds) {
            getPref(context).edit().remove(String.valueOf(widgetId)).apply();
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        for (int widgetId : appWidgetIds) {
            updateWidgets(context, appWidgetManager, widgetId);
        }
    }

    public static void updateWidgets(Context context, AppWidgetManager appWidgetManager,
            int appWidgetId) {
        long woeid = getPref(context).getLong(String.valueOf(appWidgetId), -1);
        if (woeid == -1) {
            return;
        }
        AppWidgetProviderInfo info = appWidgetManager.getAppWidgetInfo(appWidgetId);
        if (info == null) {
            // not found
            return;
        }
        int h = info.minHeight;
        int w = info.minWidth;
        float density = context.getResources().getDisplayMetrics().density;
        int hSize = (int)(Math.round(h / density) / WIDGET_UNIT);
        int wSize = (int)(Math.round(w / density) / WIDGET_UNIT);
        Log.d(TAG, "hSize: " + hSize + ", wSize: " + wSize);
        if (hSize <= 1) {
            if (wSize <= 4) {
                updateOneFourWidgetView(context, appWidgetManager, appWidgetId, woeid);
            }
        } else if (hSize == 2) {
            if (wSize <= 4) {
                updateTwoFourWidgetView(context, appWidgetManager, appWidgetId, woeid);
            }
        }
    }

    private static void updateOneFourWidgetView(Context context, AppWidgetManager appWidgetManager,
            final int appWidgetId, final long woeid) {
        WeatherData wData = UtilitiesApplication.sWeatherDataCache.get(woeid);
        if (wData == null) {
            wData = Utils.parseWeatherData(context, woeid);
            if (wData == null) {
                return;
            }
        }
        RemoteViews updateViews = new RemoteViews(context.getPackageName(),
                R.layout.weather_widget_one_four);
        // current
        updateViews.setImageViewResource(R.id.weather_widget_current_img,
                Utils.getWeatherIcon(wData.mCurrentCode));
        updateViews.setTextViewText(R.id.weather_widget_currenttmp, wData.mCurrentTemp);
        // city
        updateViews.setTextViewText(R.id.weather_data, wData.mCity);
        // forecast
        WeatherData.WeatherForecast f0 = wData.mF0;
        updateViews.setImageViewResource(R.id.weather_f0_img, Utils.getWeatherIcon(f0.mCode));
        updateViews.setTextViewText(R.id.weather_f0_date, f0.mDay);
        updateViews.setTextViewText(R.id.weather_f0_temp, f0.mLow + "/" + f0.mHigh);
        WeatherData.WeatherForecast f1 = wData.mF1;
        updateViews.setImageViewResource(R.id.weather_f1_img, Utils.getWeatherIcon(f1.mCode));
        updateViews.setTextViewText(R.id.weather_f1_date, f1.mDay);
        updateViews.setTextViewText(R.id.weather_f1_temp, f1.mLow + "/" + f0.mHigh);
        WeatherData.WeatherForecast f2 = wData.mF0;
        updateViews.setImageViewResource(R.id.weather_f2_img, Utils.getWeatherIcon(f2.mCode));
        updateViews.setTextViewText(R.id.weather_f2_date, f2.mDay);
        updateViews.setTextViewText(R.id.weather_f2_temp, f2.mLow + "/" + f2.mHigh);
        WeatherData.WeatherForecast f3 = wData.mF0;
        updateViews.setImageViewResource(R.id.weather_f3_img, Utils.getWeatherIcon(f3.mCode));
        updateViews.setTextViewText(R.id.weather_f3_date, f3.mDay);
        updateViews.setTextViewText(R.id.weather_f3_temp, f3.mLow + "/" + f3.mHigh);
        WeatherData.WeatherForecast f4 = wData.mF0;
        updateViews.setImageViewResource(R.id.weather_f4_img, Utils.getWeatherIcon(f4.mCode));
        updateViews.setTextViewText(R.id.weather_f4_date, f4.mDay);
        updateViews.setTextViewText(R.id.weather_f4_temp, f4.mLow + "/" + f4.mHigh);
        appWidgetManager.updateAppWidget(appWidgetId, updateViews);
        MixpanelTracker.getTracker(context).track(Analytics.WidgetSize.EVENT,
                Analytics.WidgetSize.SIZE_ONE_FOUR, null);
        HashMap<String, String> flurryTrackMap = new HashMap<String, String>();
        flurryTrackMap.put(Analytics.WidgetSize.SIZE_ONE_FOUR, null);
        FlurryTracker.getInstance(context).track(Analytics.WidgetSize.EVENT, flurryTrackMap);
    }

    private static void updateTwoFourWidgetView(Context context, AppWidgetManager appWidgetManager,
            final int appWidgetId, final long woeid) {
        WeatherData wData = UtilitiesApplication.sWeatherDataCache.get(woeid);
        if (wData == null) {
            wData = Utils.parseWeatherData(context, woeid);
            if (wData == null) {
                return;
            }
        }
        RemoteViews updateViews = new RemoteViews(context.getPackageName(),
                R.layout.weather_widget_two_four);
        // current
        updateViews.setImageViewResource(R.id.weather_widget_current_img,
                Utils.getWeatherIcon(wData.mCurrentCode));
        updateViews.setTextViewText(R.id.weather_widget_currenttmp, wData.mCurrentTemp + "\n"
                + wData.mCurrentCondi);
        // city
        updateViews.setTextViewText(R.id.weather_data, wData.mCity);
        // forecast
        WeatherData.WeatherForecast f0 = wData.mF0;
        updateViews.setImageViewResource(R.id.weather_f0_img, Utils.getWeatherIcon(f0.mCode));
        updateViews.setTextViewText(R.id.weather_f0_date, f0.mDay);
        updateViews.setTextViewText(R.id.weather_f0_temp, f0.mLow + "/" + f0.mHigh);
        WeatherData.WeatherForecast f1 = wData.mF1;
        updateViews.setImageViewResource(R.id.weather_f1_img, Utils.getWeatherIcon(f1.mCode));
        updateViews.setTextViewText(R.id.weather_f1_date, f1.mDay);
        updateViews.setTextViewText(R.id.weather_f1_temp, f1.mLow + "/" + f0.mHigh);
        WeatherData.WeatherForecast f2 = wData.mF0;
        updateViews.setImageViewResource(R.id.weather_f2_img, Utils.getWeatherIcon(f2.mCode));
        updateViews.setTextViewText(R.id.weather_f2_date, f2.mDay);
        updateViews.setTextViewText(R.id.weather_f2_temp, f2.mLow + "/" + f2.mHigh);
        WeatherData.WeatherForecast f3 = wData.mF0;
        updateViews.setImageViewResource(R.id.weather_f3_img, Utils.getWeatherIcon(f3.mCode));
        updateViews.setTextViewText(R.id.weather_f3_date, f3.mDay);
        updateViews.setTextViewText(R.id.weather_f3_temp, f3.mLow + "/" + f3.mHigh);
        WeatherData.WeatherForecast f4 = wData.mF0;
        updateViews.setImageViewResource(R.id.weather_f4_img, Utils.getWeatherIcon(f4.mCode));
        updateViews.setTextViewText(R.id.weather_f4_date, f4.mDay);
        updateViews.setTextViewText(R.id.weather_f4_temp, f4.mLow + "/" + f4.mHigh);
        appWidgetManager.updateAppWidget(appWidgetId, updateViews);
        MixpanelTracker.getTracker(context).track(Analytics.WidgetSize.EVENT,
                Analytics.WidgetSize.SIZE_TWO_FOUR, null);
        HashMap<String, String> flurryTrackMap = new HashMap<String, String>();
        flurryTrackMap.put(Analytics.WidgetSize.SIZE_TWO_FOUR, null);
        FlurryTracker.getInstance(context).track(Analytics.WidgetSize.EVENT, flurryTrackMap);
    }
}
