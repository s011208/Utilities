
package com.bj4.yhh.utilities.music.parser;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.bj4.yhh.utilities.music.MusicData;
import com.bj4.yhh.utilities.music.MusicDatabaseHelper;

public class U2BDataParser implements MusicDatabaseHelper.Callback {
    // https://developers.google.com/youtube/2.0/developers_guide_protocol_api_query_parameters
    // https://gdata.youtube.com/feeds/api/videos?q=五月天+入陣曲&max-results=5&alt=json&orderby=viewCount&format=6&fields=entry(id,media:group(media:content(@url,@duration)))
    // http://img.youtube.com/vi/<video id>/0.jpg
    private static final int IGNORE_DURATION = 90;

    private static final String TAG = "QQQQ";

    private static final String SOURCE_PREVIOUS = "https://gdata.youtube.com/feeds/api/videos?q=";

    private static final HandlerThread sWorkerThread = new HandlerThread("U2BDataParser handler");
    static {
        sWorkerThread.setPriority(Thread.MAX_PRIORITY);
        sWorkerThread.start();
    }

    private static final Handler sWorker = new Handler(sWorkerThread.getLooper());

    private Context mContext;

    private static U2BDataParser sInstance;

    public synchronized static final U2BDataParser getInstance(Context c) {
        if (sInstance == null) {
            sInstance = new U2BDataParser(c);
        }
        return sInstance;
    }

    private U2BDataParser(Context context) {
        mContext = context.getApplicationContext();
    }

    @Override
    public void datasetChanged() {
        startToParse();
    }

    private static final String getSource(MusicData info) {
        return SOURCE_PREVIOUS
                + Uri.encode(info.mArtist + "+" + info.mMusic)
                + "&max-results=1&alt=json&format=6&fields=entry(id,media:group(media:content(@url,@duration)))";
    }

    private void startToParse() {
        sWorker.post(new Runnable() {
            @Override
            public void run() {
                ArrayList<MusicData> musicData = MusicDatabaseHelper.getInstance(mContext)
                        .getMusicData();
                for (MusicData data : musicData) {
                    String source = getSource(data);
                    String rawData = parseOnInternet(source);
                    try {
                        JSONArray entry = new JSONObject(rawData).getJSONObject("feed")
                                .getJSONArray("entry");
                        if (entry != null) {
                            JSONObject jOb = ((JSONObject)entry.get(0));
                            data.mVideoId = jOb.getJSONObject("id").getString("$t");
                            if (data.mVideoId != null) {
                                data.mVideoId = data.mVideoId.substring(
                                        data.mVideoId.lastIndexOf("/") + 1, data.mVideoId.length());
                            }
                            data.mVideoPath = ((JSONObject)jOb.getJSONObject("media$group")
                                    .getJSONArray("media$content").get(0)).getString("url");
                            data.mRTSP = ((JSONObject)jOb.getJSONObject("media$group")
                                    .getJSONArray("media$content").get(2)).getString("url");
                        }
                        Log.d(TAG, data.toString());
                    } catch (JSONException jse) {
                        Log.w(TAG, "failed", jse);
                    }
                }
                MusicDatabaseHelper.getInstance(mContext).updateMusicData(musicData);
            }
        });

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
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (IOException ioe) {
            }
        }
        return sb.toString();
    }
}
