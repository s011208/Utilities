
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

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

public class Weather extends FrameLayout {
    private Context mContext;

    public Weather(Context context) {
        this(context, null);
    }

    public Weather(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Weather(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        mContext.startService(new Intent(mContext, WeatherService.class));
        init();
    }

    private void init() {
        View contentView = ((LayoutInflater)mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.weather, null);
        ListView mWeatherList = (ListView)contentView.findViewById(R.id.weather_data_list);
        final WeatherListAdapter mAdapter = new WeatherListAdapter(mContext);
        mWeatherList.setAdapter(mAdapter);
        addView(contentView);
    }

    public static class WeatherListAdapter extends BaseAdapter {
        private Context mContext;

        private ArrayList<WeatherData> mData;

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
        public WeatherData getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
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
        }

        public static class LoadWeatherDataTask extends AsyncTask<Void, Void, Void> {
            private static String readFromFile(String filePath) {
                String ret = "";
                try {
                    BufferedReader br = new BufferedReader(new FileReader(filePath));
                    StringBuilder sb = new StringBuilder();
                    String line = br.readLine();

                    while (line != null) {
                        sb.append(line);
                        sb.append(System.lineSeparator());
                        line = br.readLine();
                    }
                    ret = sb.toString();
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return ret;
            }

            private long mWoeid;

            private ViewHolder mHolder;

            private String mWeatherLocation, mWeatherCurrentCondition, mWeatherCurrentSubCondition,
                    mForecast0, mForecast1, mForecast2, mForecast3, mForecast4;

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
                File file = new File(mContext.getFilesDir().getAbsolutePath() + File.separator
                        + WeatherService.FOLDER_NAME + File.separator + mWoeid);
                if (file.exists()) {
                    String data = readFromFile(file.getAbsolutePath());
                    try {
                        JSONObject channel = new JSONObject(data).getJSONObject("query")
                                .getJSONObject("results").getJSONObject("channel");
                        JSONObject location = channel.getJSONObject("location");
                        mWeatherLocation = location.getString("city") + " ,"
                                + location.getString("country");

                        JSONObject wind = channel.getJSONObject("wind");
                        mWeatherCurrentSubCondition = "wind: " + wind.getString("speed");

                        JSONObject atmosphere = channel.getJSONObject("atmosphere");
                        mWeatherCurrentSubCondition += "\nhumidity: "
                                + atmosphere.getString("humidity") + "\nvisibility: "
                                + atmosphere.getString("visibility");

                        JSONObject astronomy = channel.getJSONObject("astronomy");
                        mWeatherCurrentSubCondition += "\nsunrise: "
                                + astronomy.getString("sunrise") + "\nsunset: "
                                + astronomy.getString("sunset");

                        JSONObject condition = channel.getJSONObject("item").getJSONObject(
                                "condition");
                        mWeatherCurrentCondition = condition.getString("temp") + "\n"
                                + condition.getString("text");

                        JSONArray forecast = channel.getJSONObject("item").getJSONArray("forecast");
                        for (int i = 0; i < forecast.length(); i++) {
                            JSONObject f = forecast.getJSONObject(i);
                            if (i == 0) {
                                mForecast0 = f.getString("day") + "\n" + f.getString("high")
                                        + " / " + f.getString("low");
                            } else if (i == 1) {
                                mForecast1 = f.getString("day") + "\n" + f.getString("high")
                                        + " / " + f.getString("low");
                            } else if (i == 2) {
                                mForecast2 = f.getString("day") + "\n" + f.getString("high")
                                        + " / " + f.getString("low");
                            } else if (i == 3) {
                                mForecast3 = f.getString("day") + "\n" + f.getString("high")
                                        + " / " + f.getString("low");
                            } else if (i == 4) {
                                mForecast4 = f.getString("day") + "\n" + f.getString("high")
                                        + " / " + f.getString("low");
                            }
                        }
                    } catch (JSONException e) {
                        Log.w("QQQQ", "failed", e);
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void v) {
                mHolder.mWeatherLocation.setText(mWeatherLocation);
                mHolder.mWeatherCurrentCondition.setText(mWeatherCurrentCondition);
                mHolder.mWeatherCurrentSubCondition.setText(mWeatherCurrentSubCondition);
                mHolder.mForecast0.setText(mForecast0);
                mHolder.mForecast1.setText(mForecast1);
                mHolder.mForecast2.setText(mForecast2);
                mHolder.mForecast3.setText(mForecast3);
                mHolder.mForecast4.setText(mForecast4);
            }
        }

    }

}