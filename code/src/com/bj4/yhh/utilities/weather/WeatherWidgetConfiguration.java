
package com.bj4.yhh.utilities.weather;

import java.util.ArrayList;

import com.bj4.yhh.utilities.DatabaseHelper;
import com.bj4.yhh.utilities.R;
import com.bj4.yhh.utilities.UtilitiesApplication;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class WeatherWidgetConfiguration extends Activity {
    private ListView mWeatherList;

    private WeatherWidgetConfigListAdapter mAdapter;

    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    private SharedPreferences mPref;

    public static final String WIDGET_PROVIDER_DATA = "widget_provider_data";

    public static final int CURRENT_LOCATION = -100;

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
        mAdapter = new WeatherWidgetConfigListAdapter(this);
        mWeatherList.setAdapter(mAdapter);
        mWeatherList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                AppWidgetManager appWidgetManager = AppWidgetManager
                        .getInstance(WeatherWidgetConfiguration.this);
                long woeid = mAdapter.getItem(arg2).mWoeid;
                mPref.edit().putLong(String.valueOf(mAppWidgetId), woeid).commit();
                WeatherWidget
                        .updateWidgets(getApplicationContext(), appWidgetManager, mAppWidgetId);
                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                setResult(RESULT_OK, resultValue);
                finish();
            }
        });
    }

    public static class WeatherWidgetConfigListAdapter extends BaseAdapter {
        private Context mContext;

        private ArrayList<WeatherWoeId> mData;

        private LayoutInflater mInflater;

        public WeatherWidgetConfigListAdapter(Context c) {
            mContext = c;
            mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            init();
        }

        private void init() {
            mData = DatabaseHelper.getInstance(mContext).getWeatherWoeid();
            mData.add(0, new WeatherWoeId(CURRENT_LOCATION, 0));
        }

        public void notifyDataSetChanged() {
            init();
            super.notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public WeatherWoeId getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.weather_setting_item, null);
                holder = new ViewHolder();
                holder.mListItem = (TextView)convertView.findViewById(R.id.weather_setting_item);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }
            if (position == 0) {
                holder.mListItem.setText(R.string.current_location);
            } else {
                WeatherWoeId woeidData = getItem(position);
                WeatherData wData = UtilitiesApplication.sWeatherDataCache.get(woeidData.mWoeid);
                if (wData == null) {
                    holder.mListItem.setText(woeidData.mWoeid + "");
                } else {
                    setListItemContent(holder.mListItem, wData);
                }
            }
            return convertView;
        }

        private static void setListItemContent(TextView txt, WeatherData wData) {
            if (txt == null || wData == null) {
                return;
            }
            txt.setText(wData.mCity + ", " + wData.mCountry);
        }

        public static class ViewHolder {
            TextView mListItem;
        }

    }
}
