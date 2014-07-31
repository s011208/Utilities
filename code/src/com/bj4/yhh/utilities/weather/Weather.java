
package com.bj4.yhh.utilities.weather;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.bj4.yhh.utilities.DatabaseHelper;
import com.bj4.yhh.utilities.R;
import com.bj4.yhh.utilities.UtilitiesApplication;
import com.bj4.yhh.utilities.util.Utils;
import com.bj4.yhh.utilities.weather.Weather.WeatherListAdapter.ViewHolder;

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class Weather extends FrameLayout {
    private static final boolean DEBUG = true;

    private static final String TAG = "Weather";

    public static final String INTENT_ON_DATA_UPDATE = "com.bj4.yhh.utilities.weather.on_data_update";

    public static final String INTENT_ON_ID_UPDATE = "com.bj4.yhh.utilities.weather.on_id_update";

    public static final String INTENT_EXTRAS_ON_ID_UPDATE_RESULT = "com.bj4.yhh.utilities.weather.on_id_update_result";

    private Context mContext;

    private WeatherListAdapter mAdapter;

    private FragmentManager mFragmentManager;

    public Weather(Context context) {
        this(context, null);
    }

    public Weather(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Weather(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        init();
    }

    public void setFragmentManager(FragmentManager fm) {
        mFragmentManager = fm;
    }

    public void updateContent() {
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    private void init() {
        View contentView = ((LayoutInflater)mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.weather, null);
        ListView mWeatherList = (ListView)contentView.findViewById(R.id.weather_data_list);
        mAdapter = new WeatherListAdapter(mContext);
        mWeatherList.setAdapter(mAdapter);
        mWeatherList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            }
        });
        mWeatherList.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (mFragmentManager != null) {
                    final long woeid = mAdapter.getItem(position).mWoeid;
                    WeatherData wData = UtilitiesApplication.sWeatherDataCache.get(woeid);
                    WeatherRemoveDialog dialog = WeatherRemoveDialog.getNewInstance(wData,
                            new WeatherRemoveDialog.Callback() {

                                @Override
                                public void onPositiveClick() {
                                    DatabaseHelper.getInstance(mContext).removeWoeid(woeid);
                                    mAdapter.notifyDataSetChanged();
                                }
                            });
                    dialog.show(mFragmentManager, "WeatherRemoveDialog");
                    return true;
                }
                return false;
            }
        });
        addView(contentView);
    }

    public static class WeatherListAdapter extends BaseAdapter {
        private Context mContext;

        private ArrayList<WeatherWoeId> mData;

        private LayoutInflater mInflater;

        public WeatherListAdapter(Context c) {
            mContext = c;
            mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            init();
        }

        public void notifyDataSetChanged() {
            init();
            super.notifyDataSetChanged();
        }

        private void init() {
            mData = DatabaseHelper.getInstance(mContext).getWeatherWoeid();
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
            final ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.weather_item, null);
                holder = new ViewHolder();
                holder.mWeatherLocation = (TextView)convertView.findViewById(R.id.weather_location);
                holder.mWeatherCurrentCondition = (TextView)convertView
                        .findViewById(R.id.weather_current_condition);
                holder.mWeatherCurrentSubCondition = (TextView)convertView
                        .findViewById(R.id.weather_current_subcondition);
                holder.mForecast0 = (TextView)convertView.findViewById(R.id.weather_forecast_0);
                holder.mForecast1 = (TextView)convertView.findViewById(R.id.weather_forecast_1);
                holder.mForecast2 = (TextView)convertView.findViewById(R.id.weather_forecast_2);
                holder.mForecast3 = (TextView)convertView.findViewById(R.id.weather_forecast_3);
                holder.mForecast4 = (TextView)convertView.findViewById(R.id.weather_forecast_4);
                holder.mCurrentImg = (ImageView)convertView.findViewById(R.id.weather_current_img);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }
            new LoadWeatherDataTask(mContext, getItem(position).mWoeid, holder)
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            return convertView;
        }

        public static class ViewHolder {
            TextView mWeatherLocation;

            TextView mWeatherCurrentCondition;

            TextView mWeatherCurrentSubCondition;

            TextView mForecast0, mForecast1, mForecast2, mForecast3, mForecast4;

            ImageView mCurrentImg;
        }

        public static class LoadWeatherDataTask extends AsyncTask<Void, Void, Void> {

            private long mWoeid;

            private ViewHolder mHolder;

            private String mWeatherLocation, mWeatherCurrentCondition, mWeatherCurrentSubCondition,
                    mForecast0, mForecast1, mForecast2, mForecast3, mForecast4;

            private int mCurrentCode, mF0Code, mF1Code, mF2Code, mF3Code, mF4Code;

            private Context mContext;

            public LoadWeatherDataTask(Context c, long woeid, ViewHolder holder) {
                mWoeid = woeid;
                mHolder = holder;
                mContext = c;
            }

            /**
             * https://developer.yahoo.com/weather/
             */
            /**
             * https://developer.yahoo.com/yql/console/?q=show%20tables&env=
             * store://datatables.org/alltableswithkeys#h=select+*+from+weather.
             * forecast+where+woeid%3D20070568
             */
            /**
             * https://developer.yahoo.com/yql/console/?q=show%20tables&env=
             * store://datatables.org/alltableswithkeys#h=select+*+from+geo.
             * placefinder
             * +where+text%3D%2225.04%2C121.56%22+and+gflags%3D%22R%22
             */
            @Override
            protected Void doInBackground(Void... params) {
                WeatherData wData = UtilitiesApplication.sWeatherDataCache.get(mWoeid);
                String city = null, country = null, sWind = null, humidity = null, visibility = null, rise = null, set = null, currentTemp = null, currentText = null;
                WeatherData.WeatherForecast f0 = null, f1 = null, f2 = null, f3 = null, f4 = null;
                int currentCode;
                Log.d(TAG, "doInBackground: " + mWoeid);
                if (wData == null) {
                    Log.e(TAG, "wData == null: " + mWoeid);
                    wData = Utils.parseWeatherData(mContext, mWoeid);
                }
                if (mHolder != null && wData != null) {
                    city = wData.mCity;
                    country = wData.mCountry;
                    sWind = wData.mWind;
                    humidity = wData.mHumidity;
                    visibility = wData.mVisibility;
                    rise = wData.mSunrise;
                    set = wData.mSunset;
                    currentTemp = wData.mCurrentTemp;
                    currentText = wData.mCurrentCondi;
                    currentCode = wData.mCurrentCode;
                    f0 = wData.mF0;
                    f1 = wData.mF1;
                    f2 = wData.mF2;
                    f3 = wData.mF3;
                    f4 = wData.mF4;
                    if (city != null) {
                        mWeatherLocation = city + " ," + country;
                        mWeatherCurrentSubCondition = "wind: " + sWind;
                        mWeatherCurrentSubCondition += "\nhumidity: " + humidity + "\nvisibility: "
                                + visibility;
                        mWeatherCurrentSubCondition += "\nsunrise: " + rise + "\nsunset: " + set;
                        mWeatherCurrentCondition = currentTemp + "\n" + currentText;
                        mForecast0 = f0.mDay + "\n" + f0.mHigh + " / " + f0.mLow;
                        mForecast1 = f1.mDay + "\n" + f1.mHigh + " / " + f1.mLow;
                        mForecast2 = f2.mDay + "\n" + f2.mHigh + " / " + f2.mLow;
                        mForecast3 = f3.mDay + "\n" + f3.mHigh + " / " + f3.mLow;
                        mForecast4 = f4.mDay + "\n" + f4.mHigh + " / " + f4.mLow;
                        mCurrentCode = currentCode;
                        mF0Code = f0.mCode;
                        mF1Code = f1.mCode;
                        mF2Code = f2.mCode;
                        mF3Code = f3.mCode;
                        mF4Code = f4.mCode;
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void v) {
                if (mHolder != null) {
                    mHolder.mWeatherLocation.setText(mWeatherLocation);
                    mHolder.mWeatherCurrentCondition.setText(mWeatherCurrentCondition);
                    mHolder.mWeatherCurrentSubCondition.setText(mWeatherCurrentSubCondition);
                    mHolder.mForecast0.setText(mForecast0);
                    mHolder.mForecast1.setText(mForecast1);
                    mHolder.mForecast2.setText(mForecast2);
                    mHolder.mForecast3.setText(mForecast3);
                    mHolder.mForecast4.setText(mForecast4);
                    mHolder.mCurrentImg.setImageResource(Utils.getWeatherIcon(mCurrentCode));
                }
            }
        }

    }

}
