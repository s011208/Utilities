
package com.bj4.yhh.utilities.weather;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.bj4.yhh.utilities.DatabaseHelper;
import com.bj4.yhh.utilities.UtilitiesApplication;
import com.bj4.yhh.utilities.util.Utils;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

public class WeatherService extends Service {
    private static final boolean DEBUG = false;

    private static final String TAG = "WeatherService";

    public static final String FOLDER_NAME = "weather_data";

    public static final String INTENT_KEY_WOEID = "get_woeid";

    public static final String INTENT_KEY_LAT = "get_lat";

    public static final String INTENT_KEY_LON = "get_lon";

    public static final String INTENT_UPDATE_ALL = "update_all";

    private File mRoot;

    private final ArrayList<Long> mParsingWoeid = new ArrayList<Long>();

    @Override
    public void onCreate() {
        super.onCreate();
        createFolderIfNeeded();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                boolean updateAll = extras.getBoolean(INTENT_UPDATE_ALL, false);
                if (updateAll) {
                    Log.d(TAG, "update all");
                    parseAllWeatherData(DatabaseHelper.getInstance(this).getWeatherWoeid());
                } else {
                    long woeid = extras.getLong(INTENT_KEY_WOEID);
                    float lat = extras.getFloat(INTENT_KEY_LAT);
                    float lon = extras.getFloat(INTENT_KEY_LON);
                    if (woeid != 0) {
                        Log.d(TAG, "update woeid");
                        parseWeatherData(woeid);
                    } else if (lat != DatabaseHelper.TABLE_CITIES_LIST_NOT_FOUND_DATA
                            && lon != DatabaseHelper.TABLE_CITIES_LIST_NOT_FOUND_DATA) {
                        Log.d(TAG, "update city id");
                        parseCityIdData(lat, lon);
                    }
                }
            }
        }
        return Service.START_NOT_STICKY;
    }

    private void parseCityIdData(float lat, float lon) {
        new WeatherIdParserTask(lat, lon, new ParseDoneCallback() {
            @Override
            public void done(long woeid) {
                if (woeid != 0) {
                    DatabaseHelper.getInstance(WeatherService.this).addNewWoeid(woeid);
                    parseWeatherData(woeid);
                }
                Intent intent = new Intent(Weather.INTENT_ON_ID_UPDATE);
                intent.putExtra(Weather.INTENT_EXTRAS_ON_ID_UPDATE_RESULT, woeid != 0);
                WeatherService.this.sendBroadcast(intent);
            }
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void parseWeatherData(long woeid) {
        if (DEBUG)
            Log.d(TAG, "request to parse: " + woeid);
        if (mParsingWoeid.contains(woeid) == false) {
            mParsingWoeid.add(woeid);
            new WeatherDataParserTask(woeid, mRoot, new ParseDoneCallback() {
                @Override
                public void done(long woeid) {
                    mParsingWoeid.remove(woeid);
                    WeatherData wData = Utils.parseWeatherData(WeatherService.this, woeid);
                    if (wData != null) {
                        UtilitiesApplication.sWeatherDataCache.put(woeid, wData);
                    }
                    WeatherService.this.sendBroadcast(new Intent(Weather.INTENT_ON_DATA_UPDATE));
                }
            }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            Log.v(TAG, "woeid: " + woeid + " is parsing");
        }
    }

    private void parseAllWeatherData(ArrayList<WeatherWoeId> woeids) {
        if (woeids == null) {
            return;
        }
        for (int i = 0; i < woeids.size(); i++) {
            long woeid = woeids.get(i).mWoeid;
            if (mParsingWoeid.contains(woeid) == false) {
                mParsingWoeid.add(woeid);
                new WeatherDataParserTask(woeid, mRoot, new ParseDoneCallback() {
                    @Override
                    public void done(long woeid) {
                        mParsingWoeid.remove(woeid);
                        WeatherData wData = Utils.parseWeatherData(WeatherService.this, woeid);
                        if (wData != null) {
                            UtilitiesApplication.sWeatherDataCache.put(woeid, wData);
                        }
                        if (mParsingWoeid.isEmpty()) {
                            WeatherService.this.sendBroadcast(new Intent(
                                    Weather.INTENT_ON_DATA_UPDATE));
                        }
                    }
                }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                Log.v(TAG, "woeid: " + woeid + " is parsing");
            }
        }
    }

    public interface ParseDoneCallback {
        public void done(long woeid);
    }

    public static class WeatherIdParserTask extends AsyncTask<Void, Void, Void> {
        private ParseDoneCallback mCallback;

        private float mLat, mLon;

        private Long mWoeid = 0l;

        public WeatherIdParserTask(float lat, float lon, ParseDoneCallback callback) {
            mLat = lat;
            mLon = lon;
            mCallback = callback;
        }

        @Override
        protected Void doInBackground(Void... params) {
            String url = "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20geo.placefinder%20where%20text%3D%22"
                    + mLat
                    + "%2C"
                    + mLon
                    + "%22%20and%20gflags%3D%22R%22&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=";
            String data = Utils.parseOnInternet(url);
            try {
                JSONObject result = new JSONObject(data).getJSONObject("query")
                        .getJSONObject("results").getJSONObject("Result");
                mWoeid = result.getLong("woeid");
            } catch (JSONException e) {
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            if (mCallback != null) {
                mCallback.done(mWoeid);
            }
        }
    }

    public static class WeatherDataParserTask extends AsyncTask<Void, Void, Void> {

        private ParseDoneCallback mCallback;

        private Long mWoeid = 0l;

        private File mRoot;

        public WeatherDataParserTask(Long woeid, File root, ParseDoneCallback cb) {
            mWoeid = woeid;
            mRoot = root;
            mCallback = cb;
        }

        @Override
        protected Void doInBackground(Void... params) {
            String url = "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20weather.forecast%20where%20woeid%3D"
                    + mWoeid
                    + "&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=";
            String data = Utils.parseOnInternet(url);
            if (data != null && TextUtils.isEmpty(data) == false) {
                File file = new File(mRoot.getAbsolutePath() + File.separator + mWoeid);
                if (file.exists()) {
                    file.delete();
                }
                try {
                    file.createNewFile();
                    Utils.writeToFile(file.getAbsolutePath(), data);
                } catch (IOException e) {
                }
                if (DEBUG)
                    Log.d(TAG, data);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            if (mCallback != null) {
                mCallback.done(mWoeid);
            }
        }

    }

    private void createFolderIfNeeded() {
        mRoot = new File(getFilesDir().getAbsolutePath() + File.separator + FOLDER_NAME);
        if (mRoot.exists() == false) {
            mRoot.mkdir();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
