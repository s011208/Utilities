
package com.bj4.yhh.utilities.music.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.bj4.yhh.utilities.music.MusicDatabaseHelper;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

public class KKBoxParser implements Runnable {
    private static final boolean DEBUG = true;

    private static final String TAG = "QQQQ";

    private static final HashMap<String, String> URLS = new HashMap<String, String>();
    static {
        URLS.put("華語電台", "http://www.kkbox.com/tw/tc/charts/chinese-daily-song-latest.html");
        URLS.put("西洋電台", "http://www.kkbox.com/tw/tc/charts/western-daily-song-latest.html");
        URLS.put("日語電台", "http://www.kkbox.com/tw/tc/charts/japanese-daily-song-latest.html");
        URLS.put("韓語電台", "http://www.kkbox.com/tw/tc/charts/korean-daily-song-latest.html");
        URLS.put("嘻哈電台", "http://www.kkbox.com/tw/tc/charts/hiphop_rnb-daily-song-latest.html");
        URLS.put("搖滾電台", "http://www.kkbox.com/tw/tc/charts/rock-daily-song-latest.html");
        URLS.put("電子電台", "http://www.kkbox.com/tw/tc/charts/electronic-daily-song-latest.html");
        URLS.put("古典電台", "http://www.kkbox.com/tw/tc/charts/classical-daily-song-latest.html");
        URLS.put("爵士電台", "http://www.kkbox.com/tw/tc/charts/jazz-daily-song-latest.html");
        URLS.put("療癒電台", "http://www.kkbox.com/tw/tc/charts/world_music-daily-song-latest.html");
    }

    private Context mContext;

    public KKBoxParser(Context c) {
        mContext = c;
    }

    @Override
    public void run() {
        if (DEBUG)
            Log.d(TAG, "start to parse kkb");
        Iterator<String> iter = URLS.keySet().iterator();
        ArrayList<ContentValues> cvs = new ArrayList<ContentValues>();
        ArrayList<String> successMusicType = new ArrayList<String>();
        while (iter.hasNext()) {
            String musicType = iter.next();
            String url = URLS.get(musicType);
            try {
                Document doc = Jsoup.connect(url).get();
                Elements items = doc.select("div[class$=item]");
                for (Element item : items) {
                    Elements h4 = item.select("h4");
                    Elements h5 = item.select("h5");
                    ContentValues cv = new ContentValues();
                    cv.put(MusicDatabaseHelper.MSUIC_DATA_MUSIC, h4.text());
                    cv.put(MusicDatabaseHelper.MUSIC_DATA_ARTIST, h5.text());
                    cv.put(MusicDatabaseHelper.MUSIC_DATA_TYPE, musicType);
                    cvs.add(cv);
                }
                successMusicType.add(musicType);
            } catch (IOException e) {
                if (DEBUG)
                    Log.w(TAG, "failed", e);
            }
        }
        MusicDatabaseHelper.getInstance(mContext).addMusicData(cvs, successMusicType, true);
    }
}
