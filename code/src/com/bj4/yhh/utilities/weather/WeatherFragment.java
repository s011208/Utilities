
package com.bj4.yhh.utilities.weather;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.bj4.yhh.utilities.UpdateManagerService;
import com.bj4.yhh.utilities.fragments.BaseFragment;

public class WeatherFragment extends BaseFragment implements Weather.RequestCallback {

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (Weather.INTENT_ON_DATA_UPDATE.equals(intent.getAction())) {
                if (mContentView != null) {
                    ((Weather)mContentView).updateContent();
                }
            }
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntentFilter filter = new IntentFilter(Weather.INTENT_ON_DATA_UPDATE);
        mContext.registerReceiver(mReceiver, filter);
    }

    public void onResume() {
        super.onResume();
        if (mContentView != null) {
            ((Weather)mContentView).updateContent();
        }
    }

    public void onDestroy() {
        mContext.unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    @Override
    public void init() {
        mContentView = new Weather(mContext);
        ((Weather)mContentView).setFragmentManager(getFragmentManager());
        ((Weather)mContentView).setCallback(this);
    }

    @Override
    public void requestUpdate() {
        Intent startIntent = new Intent(mContext, UpdateManagerService.class);
        startIntent.putExtra(UpdateManagerService.UPDATE_TYPE,
                UpdateManagerService.UPDATE_TYPE_WEATHER);
        mContext.startService(startIntent);
    }

}
