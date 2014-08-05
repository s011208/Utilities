
package com.bj4.yhh.utilities.weather;

import com.bj4.yhh.utilities.R;
import com.bj4.yhh.utilities.weather.WeatherSettingDialog.WeatherListAdapter;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class WeatherWidgetConfiguration extends Activity {
    private ListView mWeatherList;

    private WeatherListAdapter mAdapter;

    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    private SharedPreferences mPref;

    public static final String WIDGET_PROVIDER_DATA = "widget_provider_data";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_widget_configuration);
        mPref = getSharedPreferences(WIDGET_PROVIDER_DATA, Context.MODE_PRIVATE);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        mWeatherList = (ListView)findViewById(R.id.weather_configuration_listview);
        mAdapter = new WeatherListAdapter(this);
        mWeatherList.setAdapter(mAdapter);
        mWeatherList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                AppWidgetManager appWidgetManager = AppWidgetManager
                        .getInstance(WeatherWidgetConfiguration.this);
                long woeid = mAdapter.getItem(arg2).mWoeid;
                mPref.edit().putLong(String.valueOf(mAppWidgetId), woeid).commit();
                WeatherWidget.updateWidgets(getApplicationContext(), appWidgetManager, mAppWidgetId);
                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                setResult(RESULT_OK, resultValue);
                finish();
            }
        });
    }
}
