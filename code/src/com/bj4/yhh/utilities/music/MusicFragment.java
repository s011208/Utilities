
package com.bj4.yhh.utilities.music;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.bj4.yhh.utilities.fragments.BaseFragment;

public class MusicFragment extends BaseFragment {
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (Music.INTENT_ON_DATA_UPDATE.equals(intent.getAction())) {
                if (mContentView != null) {
                    ((Music)mContentView).updateContent();
                }
            }
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntentFilter filter = new IntentFilter(Music.INTENT_ON_DATA_UPDATE);
        mContext.registerReceiver(mReceiver, filter);
    }

    public void onResume() {
        super.onResume();
        if (mContentView != null) {
            ((Music)mContentView).updateContent();
        }
    }

    public void onDestroy() {
        mContext.unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    @Override
    public void init() {
        mContentView = new Music(mContext);
    }

}
