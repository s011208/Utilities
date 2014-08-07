
package com.bj4.yhh.utilities.weather;

import java.util.HashMap;

import com.bj4.yhh.utilities.R;
import com.bj4.yhh.utilities.SettingManager;
import com.bj4.yhh.utilities.UtilitiesApplication;
import com.bj4.yhh.utilities.analytics.Analytics;
import com.bj4.yhh.utilities.analytics.flurry.FlurryTracker;
import com.bj4.yhh.utilities.analytics.googleanalytics.GoogleAnalyticsTracker;
import com.bj4.yhh.utilities.analytics.mixpanel.MixpanelTracker;
import com.bj4.yhh.utilities.util.Utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetProviderInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.text.format.Time;
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

    private void setUpdateAlarm(Context context) {
        final Intent intent = new Intent(context, WeatherWidgetUpdateService.class);
        final PendingIntent pending = PendingIntent.getService(context, 0, intent, 0);
        final AlarmManager alarm = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pending);
        long interval = 1000 * 60;
        alarm.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), interval,
                pending);
    }

    private BroadcastReceiver mTimeTickReceiver;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        for (int widgetId : appWidgetIds) {
            updateWidgets(context, appWidgetManager, widgetId);
        }
        if (mTimeTickReceiver == null) {
            mTimeTickReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    setUpdateAlarm(context.getApplicationContext());
                    context.getApplicationContext().unregisterReceiver(this);
                }
            };
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIME_TICK);
            context.getApplicationContext().registerReceiver(mTimeTickReceiver, filter);
            setUpdateAlarm(context.getApplicationContext());
        }
    }

    public static void updateWidgets(Context context, AppWidgetManager appWidgetManager,
            int appWidgetId) {
        long woeid = getPref(context).getLong(String.valueOf(appWidgetId), -1);
        if (woeid == WeatherWidgetConfiguration.CURRENT_LOCATION) {
            woeid = SettingManager.getInstance(context).getCurrentLocationWoeid();
        }
        if (woeid == -1) {
            updateFailedToGetWeatherData(context, appWidgetManager, appWidgetId);
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

    private static void updateFailedToGetWeatherData(Context context,
            AppWidgetManager appWidgetManager, final int appWidgetId) {
        RemoteViews updateViews = new RemoteViews(context.getPackageName(),
                R.layout.weather_widget_fail_to_get_data);
        appWidgetManager.updateAppWidget(appWidgetId, updateViews);
        MixpanelTracker.getTracker(context).track(Analytics.WidgetSize.EVENT,
                Analytics.WidgetSize.FAILED_TO_GET_DATA, Analytics.WidgetSize.FAILED_TO_GET_DATA);
        HashMap<String, String> flurryTrackMap = new HashMap<String, String>();
        flurryTrackMap.put(Analytics.WidgetSize.FAILED_TO_GET_DATA,
                Analytics.WidgetSize.FAILED_TO_GET_DATA);
        FlurryTracker.getInstance(context).track(Analytics.WidgetSize.EVENT, flurryTrackMap);
        GoogleAnalyticsTracker.getInstance(context).sendEvents(context, Analytics.WidgetSize.EVENT,
                Analytics.WidgetSize.FAILED_TO_GET_DATA, null, null);
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
        WeatherData.WeatherForecast f2 = wData.mF2;
        updateViews.setImageViewResource(R.id.weather_f2_img, Utils.getWeatherIcon(f2.mCode));
        updateViews.setTextViewText(R.id.weather_f2_date, f2.mDay);
        updateViews.setTextViewText(R.id.weather_f2_temp, f2.mLow + "/" + f2.mHigh);
        WeatherData.WeatherForecast f3 = wData.mF3;
        updateViews.setImageViewResource(R.id.weather_f3_img, Utils.getWeatherIcon(f3.mCode));
        updateViews.setTextViewText(R.id.weather_f3_date, f3.mDay);
        updateViews.setTextViewText(R.id.weather_f3_temp, f3.mLow + "/" + f3.mHigh);
        WeatherData.WeatherForecast f4 = wData.mF4;
        updateViews.setImageViewResource(R.id.weather_f4_img, Utils.getWeatherIcon(f4.mCode));
        updateViews.setTextViewText(R.id.weather_f4_date, f4.mDay);
        updateViews.setTextViewText(R.id.weather_f4_temp, f4.mLow + "/" + f4.mHigh);
        appWidgetManager.updateAppWidget(appWidgetId, updateViews);
        MixpanelTracker.getTracker(context).track(Analytics.WidgetSize.EVENT,
                Analytics.WidgetSize.SIZE_ONE_FOUR, Analytics.WidgetSize.SIZE_ONE_FOUR);
        HashMap<String, String> flurryTrackMap = new HashMap<String, String>();
        flurryTrackMap.put(Analytics.WidgetSize.SIZE_ONE_FOUR, Analytics.WidgetSize.SIZE_ONE_FOUR);
        FlurryTracker.getInstance(context).track(Analytics.WidgetSize.EVENT, flurryTrackMap);
        GoogleAnalyticsTracker.getInstance(context).sendEvents(context, Analytics.WidgetSize.EVENT,
                Analytics.WidgetSize.SIZE_ONE_FOUR, null, null);
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
        String tempUnit = SettingManager.getInstance(context).isUsingC() ? "˚C" : "˚F";
        updateViews.setTextViewText(R.id.current_temp, wData.mCurrentTemp + " " + tempUnit);
        updateViews.setTextViewText(R.id.current_condition, wData.mCurrentCondi);
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
        WeatherData.WeatherForecast f2 = wData.mF2;
        updateViews.setImageViewResource(R.id.weather_f2_img, Utils.getWeatherIcon(f2.mCode));
        updateViews.setTextViewText(R.id.weather_f2_date, f2.mDay);
        updateViews.setTextViewText(R.id.weather_f2_temp, f2.mLow + "/" + f2.mHigh);
        WeatherData.WeatherForecast f3 = wData.mF3;
        updateViews.setImageViewResource(R.id.weather_f3_img, Utils.getWeatherIcon(f3.mCode));
        updateViews.setTextViewText(R.id.weather_f3_date, f3.mDay);
        updateViews.setTextViewText(R.id.weather_f3_temp, f3.mLow + "/" + f3.mHigh);
        WeatherData.WeatherForecast f4 = wData.mF4;
        updateViews.setImageViewResource(R.id.weather_f4_img, Utils.getWeatherIcon(f4.mCode));
        updateViews.setTextViewText(R.id.weather_f4_date, f4.mDay);
        updateViews.setTextViewText(R.id.weather_f4_temp, f4.mLow + "/" + f4.mHigh);
        // time
        updateViews.setTextViewText(R.id.current_date, getFullDateString());
        updateViews.setTextViewText(R.id.current_time, getFullTimeString());
        // sunrise/set
        String sunrise = wData.mSunrise.replaceAll("pm|am", "");
        String sunset = wData.mSunset.replaceAll("pm|am", "");
        updateViews.setTextViewText(R.id.sun_time, "sunrise: " + sunrise + "  sunset: " + sunset);
        appWidgetManager.updateAppWidget(appWidgetId, updateViews);
        MixpanelTracker.getTracker(context).track(Analytics.WidgetSize.EVENT,
                Analytics.WidgetSize.SIZE_TWO_FOUR, Analytics.WidgetSize.SIZE_TWO_FOUR);
        HashMap<String, String> flurryTrackMap = new HashMap<String, String>();
        flurryTrackMap.put(Analytics.WidgetSize.SIZE_TWO_FOUR, Analytics.WidgetSize.SIZE_TWO_FOUR);
        FlurryTracker.getInstance(context).track(Analytics.WidgetSize.EVENT, flurryTrackMap);
        GoogleAnalyticsTracker.getInstance(context).sendEvents(context, Analytics.WidgetSize.EVENT,
                Analytics.WidgetSize.SIZE_TWO_FOUR, null, null);
    }

    private static String getFullTimeString() {
        Time now = new Time();
        now.setToNow();
        int h = now.hour;
        int m = now.minute;
        String hour = String.valueOf(h);
        if (hour.length() < 2)
            hour = "0" + hour;
        String minute = String.valueOf(m);
        if (minute.length() < 2) {
            minute = "0" + minute;
        }
        return hour + " : " + minute;
    }

    private static String getYear(Time now) {
        return String.valueOf(now.year);
    }

    private static String getMonth(Time now) {
        String m = null;
        switch (now.month) {
            case 0:
                m = "Jan";
                break;
            case 1:
                m = "Feb";
                break;
            case 2:
                m = "Mar";
                break;
            case 3:
                m = "Apr";
                break;
            case 4:
                m = "May";
                break;
            case 5:
                m = "Jun";
                break;
            case 6:
                m = "Jul";
                break;
            case 7:
                m = "Aug";
                break;
            case 8:
                m = "Sep";
                break;
            case 9:
                m = "Oct";
                break;
            case 10:
                m = "Nov";
                break;
            case 11:
                m = "Dec";
                break;
        }
        return m;
    }

    private static String getDay(Time now) {
        return String.valueOf(now.monthDay);
    }

    private static String getWeekDay(Time now) {
        String w = null;
        switch (now.weekDay) {
            case 0:
                w = "Sun";
                break;
            case 1:
                w = "Mon";
                break;
            case 2:
                w = "Tue";
                break;
            case 3:
                w = "Wed";
                break;
            case 4:
                w = "Thu";
                break;
            case 5:
                w = "Fri";
                break;
            case 6:
                w = "Sat";
                break;
        }
        return w;
    }

    private static String getFullDateString() {
        Time now = new Time();
        now.setToNow();
        return getMonth(now) + " " + getDay(now) + ", " + getYear(now) + " - " + getWeekDay(now);
    }
}
