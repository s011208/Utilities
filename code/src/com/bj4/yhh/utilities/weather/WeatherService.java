
package com.bj4.yhh.utilities.weather;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;

import com.bj4.yhh.utilities.DatabaseHelper;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

public class WeatherService extends Service {
    public static final String TAG = "QQQQ";

    public static final String FOLDER_NAME = "weather_data";

    private File mRoot;

    private static int mParserCounter = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        createFolderIfNeeded();
        ArrayList<WeatherWoeId> data = DatabaseHelper.getInstance(getApplicationContext())
                .getWeatherWoeid();
        mParserCounter = data.size();
        for (WeatherWoeId woeid : data) {
            new ParserTask(woeid.mWoeid, mRoot, new ParserTask.ParseDoneCallback() {
                @Override
                public void done(long woeid) {
                    --mParserCounter;
                    if (mParserCounter <= 0) {
                        stopSelf();
                        Log.w(TAG, "stopself");
                    }
                    WeatherService.this.sendBroadcast(new Intent(Weather.INTENT_ON_DATA_UPDATE));
                }
            }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
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

        @SuppressWarnings("deprecation")
        private static String parseOnInternet(String url) {
            URL u;
            InputStream is = null;
            DataInputStream dis;
            String s;
            StringBuilder sb = new StringBuilder();
            try {
                u = new URL(url);
                is = u.openStream();
                dis = new DataInputStream(new BufferedInputStream(is));
                while ((s = dis.readLine()) != null) {
                    sb.append(s);
                }
            } catch (Exception e) {
                Log.e(TAG, "parse failed", e);
            } finally {
                try {
                    is.close();
                } catch (IOException ioe) {
                }
            }
            return sb.toString();
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
            String data = parseOnInternet(url);
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
