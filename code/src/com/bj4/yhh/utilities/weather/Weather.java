
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
import com.bj4.yhh.utilities.SettingManager;
import com.bj4.yhh.utilities.UtilitiesApplication;
import com.bj4.yhh.utilities.util.Utils;

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
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

    public interface RequestCallback {
        public void requestUpdate();
    }

    private RequestCallback mCallback;

    private Context mContext;

    private WeatherListCompositeAdapter mCompositeAdapter;

    private WeatherListSimpleAdapter mSimpleAdapter;

    private ListView mWeatherList;

    private FragmentManager mFragmentManager;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private boolean mIsUsingSimpleView = false;

    public Weather(Context context) {
        this(context, null);
    }

    public Weather(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Weather(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        mIsUsingSimpleView = SettingManager.getInstance(mContext).isWeatherUsingSimpleView();
        init();
    }

    public void setCallback(RequestCallback cb) {
        mCallback = cb;
    }

    public void setFragmentManager(FragmentManager fm) {
        mFragmentManager = fm;
    }

    public void updateContent() {
        boolean previousView = mIsUsingSimpleView;
        boolean hasChanged = false;
        mIsUsingSimpleView = SettingManager.getInstance(mContext).isWeatherUsingSimpleView();
        hasChanged = mIsUsingSimpleView != previousView;
        if (mIsUsingSimpleView) {
            if (mSimpleAdapter != null) {
                if (hasChanged)
                    mWeatherList.setAdapter(mSimpleAdapter);
                mSimpleAdapter.notifyDataSetChanged();
            } else {
                initSimpleAdapter();
            }
        } else {
            if (mCompositeAdapter != null) {
                if (hasChanged)
                    mWeatherList.setAdapter(mCompositeAdapter);
                mCompositeAdapter.notifyDataSetChanged();
            } else {
                initCompositeAdapter();
            }
        }
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void initCompositeAdapter() {
        mCompositeAdapter = new WeatherListCompositeAdapter(mContext);
        mWeatherList.setAdapter(mCompositeAdapter);
        mWeatherList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            }
        });
        mWeatherList.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (mFragmentManager != null) {
                    final long woeid = mCompositeAdapter.getItem(position).mWoeid;
                    WeatherData wData = UtilitiesApplication.sWeatherDataCache.get(woeid);
                    WeatherRemoveDialog dialog = WeatherRemoveDialog.getNewInstance(wData,
                            new WeatherRemoveDialog.Callback() {

                                @Override
                                public void onPositiveClick() {
                                    DatabaseHelper.getInstance(mContext).removeWoeid(woeid);
                                    mCompositeAdapter.notifyDataSetChanged();
                                }
                            });
                    dialog.show(mFragmentManager, "WeatherRemoveDialog");
                    return true;
                }
                return false;
            }
        });
    }

    private void initSimpleAdapter() {
        mSimpleAdapter = new WeatherListSimpleAdapter(mContext);
        mWeatherList.setAdapter(mSimpleAdapter);
    }

    private void init() {
        View contentView = ((LayoutInflater)mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.weather, null);
        mWeatherList = (ListView)contentView.findViewById(R.id.weather_data_list);
        mSwipeRefreshLayout = (SwipeRefreshLayout)contentView
                .findViewById(R.id.weather_swipe_refresh);
        mSwipeRefreshLayout.setColorScheme(android.R.color.white, android.R.color.holo_green_light,
                android.R.color.holo_orange_light, android.R.color.holo_red_light);
        mSwipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {

            @Override
            public void onRefresh() {
                if (mCallback == null) {
                    mSwipeRefreshLayout.setRefreshing(false);
                } else {
                    mCallback.requestUpdate();
                }
            }
        });
        if (mIsUsingSimpleView) {
            initSimpleAdapter();
        } else {
            initCompositeAdapter();
        }
        addView(contentView);
    }

    public static class WeatherListSimpleAdapter extends BaseAdapter {
        private Context mContext;

        private ArrayList<WeatherWoeId> mData;

        private LayoutInflater mInflater;

        public WeatherListSimpleAdapter(Context c) {
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
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.weather_simple_item, null);
                holder = new ViewHolder();
                holder.mWeatherCity = (TextView)convertView.findViewById(R.id.weather_city);
                holder.mWeatherCurrentTemp = (TextView)convertView
                        .findViewById(R.id.weather_current_temp);
                holder.mWeatherCurrentText = (TextView)convertView
                        .findViewById(R.id.weather_current_text);
                holder.mWeatherCurrentImg = (ImageView)convertView
                        .findViewById(R.id.weather_current_img);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }
            new LoadWeatherDataTask(mContext, getItem(position).mWoeid, holder)
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            return convertView;
        }

        public static class LoadWeatherDataTask extends AsyncTask<Void, Void, Void> {

            private long mWoeid;

            private ViewHolder mHolder;

            private Context mContext;

            private String mWeatherCity, mWeatherCurrentTemp, mWeatherCurrentText;

            private int mCurrentCode;

            public LoadWeatherDataTask(Context c, long woeid, ViewHolder holder) {
                mWoeid = woeid;
                mHolder = holder;
                mContext = c;
            }

            @Override
            protected Void doInBackground(Void... params) {
                WeatherData wData = UtilitiesApplication.sWeatherDataCache.get(mWoeid);
                String city = null, currentTemp = null, currentText = null;
                int currentCode;
                if (wData == null) {
                    wData = Utils.parseWeatherData(mContext, mWoeid);
                }
                if (mHolder != null && wData != null) {
                    city = wData.mCity;
                    currentTemp = wData.mCurrentTemp;
                    currentText = wData.mCurrentCondi;
                    currentCode = wData.mCurrentCode;
                    if (city != null) {
                        mWeatherCity = city;
                        mWeatherCurrentTemp = currentTemp;
                        mWeatherCurrentText = currentText;
                        mCurrentCode = currentCode;
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void v) {
                if (mHolder != null) {
                    mHolder.mWeatherCity.setText(mWeatherCity);
                    mHolder.mWeatherCurrentTemp.setText(mWeatherCurrentTemp);
                    mHolder.mWeatherCurrentText.setText(mWeatherCurrentText);
                    mHolder.mWeatherCurrentImg.setImageResource(Utils.getWeatherIcon(mCurrentCode));
                }
            }
        }

        public static class ViewHolder {
            TextView mWeatherCity, mWeatherCurrentTemp, mWeatherCurrentText;

            ImageView mWeatherCurrentImg;
        }
    }

    public static class WeatherListCompositeAdapter extends BaseAdapter {
        private Context mContext;

        private ArrayList<WeatherWoeId> mData;

        private LayoutInflater mInflater;

        public WeatherListCompositeAdapter(Context c) {
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
                convertView = mInflater.inflate(R.layout.weather_composite_item, null);
                holder = new ViewHolder();
                holder.mWeatherCity = (TextView)convertView.findViewById(R.id.weather_city);
                holder.mWeatherCountry = (TextView)convertView.findViewById(R.id.weather_country);
                holder.mWeatherCurrentTemp = (TextView)convertView
                        .findViewById(R.id.weather_current_temp);
                holder.mWeatherCurrentText = (TextView)convertView
                        .findViewById(R.id.weather_current_text);
                holder.mWeatherCurrentSubCondition = (TextView)convertView
                        .findViewById(R.id.weather_current_subcondition);
                holder.mF0Day = (TextView)convertView.findViewById(R.id.weather_f0_day);
                holder.mF0Temp = (TextView)convertView.findViewById(R.id.weather_f0_temp);
                holder.mF0Img = (ImageView)convertView.findViewById(R.id.weather_f0_img);
                holder.mF1Day = (TextView)convertView.findViewById(R.id.weather_f1_day);
                holder.mF1Temp = (TextView)convertView.findViewById(R.id.weather_f1_temp);
                holder.mF1Img = (ImageView)convertView.findViewById(R.id.weather_f1_img);
                holder.mF2Day = (TextView)convertView.findViewById(R.id.weather_f2_day);
                holder.mF2Temp = (TextView)convertView.findViewById(R.id.weather_f2_temp);
                holder.mF2Img = (ImageView)convertView.findViewById(R.id.weather_f2_img);
                holder.mF3Day = (TextView)convertView.findViewById(R.id.weather_f3_day);
                holder.mF3Temp = (TextView)convertView.findViewById(R.id.weather_f3_temp);
                holder.mF3Img = (ImageView)convertView.findViewById(R.id.weather_f3_img);
                holder.mF4Day = (TextView)convertView.findViewById(R.id.weather_f4_day);
                holder.mF4Temp = (TextView)convertView.findViewById(R.id.weather_f4_temp);
                holder.mF4Img = (ImageView)convertView.findViewById(R.id.weather_f4_img);
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
            TextView mWeatherCity, mWeatherCountry;

            TextView mWeatherCurrentTemp, mWeatherCurrentText;

            TextView mWeatherCurrentSubCondition;

            TextView mF0Day, mF0Temp, mF1Day, mF1Temp, mF2Day, mF2Temp, mF3Day, mF3Temp, mF4Day,
                    mF4Temp;

            ImageView mCurrentImg, mF0Img, mF1Img, mF2Img, mF3Img, mF4Img;
        }

        public static class LoadWeatherDataTask extends AsyncTask<Void, Void, Void> {

            private long mWoeid;

            private ViewHolder mHolder;

            private String mWeatherCity, mWeatherCountry, mWeatherCurrentTemp, mWeatherCurrentText,
                    mWeatherCurrentSubCondition, mF0Day, mF0Temp, mF1Day, mF1Temp, mF2Day, mF2Temp,
                    mF3Day, mF3Temp, mF4Day, mF4Temp;

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
                if (wData == null) {
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
                        mWeatherCity = city;
                        mWeatherCountry = country;
                        mWeatherCurrentSubCondition = "wind: " + sWind;
                        mWeatherCurrentSubCondition += "\nhumidity: " + humidity + "\nvisibility: "
                                + visibility;
                        mWeatherCurrentSubCondition += "\nsunrise: " + rise + "\nsunset: " + set;
                        mWeatherCurrentTemp = currentTemp;
                        mWeatherCurrentText = currentText;
                        mF0Day = f0.mDay;
                        mF0Temp = f0.mHigh + " / " + f0.mLow;
                        mF0Code = f0.mCode;
                        mF1Day = f1.mDay;
                        mF1Temp = f1.mHigh + " / " + f1.mLow;
                        mF1Code = f1.mCode;
                        mF2Day = f2.mDay;
                        mF2Temp = f2.mHigh + " / " + f2.mLow;
                        mF2Code = f2.mCode;
                        mF3Day = f3.mDay;
                        mF3Temp = f3.mHigh + " / " + f3.mLow;
                        mF3Code = f3.mCode;
                        mF4Day = f4.mDay;
                        mF4Temp = f4.mHigh + " / " + f4.mLow;
                        mF4Code = f4.mCode;

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
                    mHolder.mWeatherCity.setText(mWeatherCity);
                    mHolder.mWeatherCountry.setText(mWeatherCountry);
                    mHolder.mWeatherCurrentTemp.setText(mWeatherCurrentTemp);
                    mHolder.mWeatherCurrentText.setText(mWeatherCurrentText);
                    mHolder.mWeatherCurrentSubCondition.setText(mWeatherCurrentSubCondition);
                    mHolder.mF0Day.setText(mF0Day);
                    mHolder.mF0Temp.setText(mF0Temp);
                    mHolder.mF0Img.setImageResource(Utils.getWeatherIcon(mF0Code));
                    mHolder.mF1Day.setText(mF1Day);
                    mHolder.mF1Temp.setText(mF1Temp);
                    mHolder.mF1Img.setImageResource(Utils.getWeatherIcon(mF1Code));
                    mHolder.mF2Day.setText(mF2Day);
                    mHolder.mF2Temp.setText(mF2Temp);
                    mHolder.mF2Img.setImageResource(Utils.getWeatherIcon(mF2Code));
                    mHolder.mF3Day.setText(mF3Day);
                    mHolder.mF3Temp.setText(mF3Temp);
                    mHolder.mF3Img.setImageResource(Utils.getWeatherIcon(mF3Code));
                    mHolder.mF4Day.setText(mF4Day);
                    mHolder.mF4Temp.setText(mF4Temp);
                    mHolder.mF4Img.setImageResource(Utils.getWeatherIcon(mF4Code));
                    mHolder.mCurrentImg.setImageResource(Utils.getWeatherIcon(mCurrentCode));
                }
            }
        }

    }

}
