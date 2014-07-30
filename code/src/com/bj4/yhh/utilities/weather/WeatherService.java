
package com.bj4.yhh.utilities.weather;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;

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
                Long woeid = extras.getLong(INTENT_KEY_WOEID);
                if (woeid != null) {
                    if (DEBUG)
                        Log.d(TAG, "request to parse: " + woeid);
                    if (mParsingWoeid.contains(woeid) == false) {
                        mParsingWoeid.add(woeid);
                        new ParserTask(woeid, mRoot, new ParserTask.ParseDoneCallback() {
                            @Override
                            public void done(long woeid) {
                                mParsingWoeid.remove(woeid);
                                WeatherService.this.sendBroadcast(new Intent(
                                        Weather.INTENT_ON_DATA_UPDATE));
                            }
                        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    } else {
                        Log.v(TAG, "woeid: " + woeid + " is parsing");
                    }
                }
            }
        }
        return START_STICKY;
    }

    public static class ParserTask extends AsyncTask<Void, Void, Void> {

        public interface ParseDoneCallback {
            public void done(long woeid);
        }

        private ParseDoneCallback mCallback;

        private Long mWoeid;

        private File mRoot;

        public ParserTask(Long woeid, File root, ParseDoneCallback cb) {
            mWoeid = woeid;
            mRoot = root;
            mCallback = cb;
        }

        private static boolean writeToFile(final String filePath, final String data) {
            Writer writer;
            try {
                writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath),
                        "utf-8"));
                writer.write(data);
                writer.flush();
                writer.close();
                return true;
            } catch (Exception e) {
                return false;
            }
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
                    writeToFile(file.getAbsolutePath(), data);
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
