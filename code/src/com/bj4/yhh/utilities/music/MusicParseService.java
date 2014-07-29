
package com.bj4.yhh.utilities.music;

import com.bj4.yhh.utilities.music.parser.KKBoxParser;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class MusicParseService extends Service {
    @Override
    public void onCreate() {
        super.onCreate();
        new Thread(new KKBoxParser(this)).start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
