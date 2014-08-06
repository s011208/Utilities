
package com.bj4.yhh.utilities.analytics.mixpanel;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.bj4.yhh.utilities.SettingManager;
import com.bj4.yhh.utilities.analytics.Analytics;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

public class MixpanelTracker {
    private static final String MIXPANEL_TOKEN = "c70b09b71105c532fec770a2bd03bd21";

    private static MixpanelTracker sInstance;

    public synchronized static MixpanelTracker getTracker(Context context) {
        if (sInstance == null) {
            sInstance = new MixpanelTracker(context);
        }
        return sInstance;
    }

    private boolean mEnableTracker = Analytics.ENABLE_TRACKER;

    // Mixpanel
    private MixpanelAPI mMixpanel;

    private MixpanelTracker(Context context) {
        mEnableTracker = SettingManager.getInstance(context).isEnableGa();
        mMixpanel = MixpanelAPI.getInstance(context.getApplicationContext(), MIXPANEL_TOKEN);
    }

    public void checkEnableState() {
        mEnableTracker = Analytics.ENABLE_TRACKER;
    }

    public void track(String eventName, String property, String value) {
        if (!mEnableTracker)
            return;
        JSONObject properties = new JSONObject();
        try {
            properties.put(property, value);
            mMixpanel.track(eventName, properties);
        } catch (JSONException e) {
        }
    }

    public void track(String eventName, JSONObject properties) {
        if (!mEnableTracker)
            return;
        mMixpanel.track(eventName, properties);
    }

    public boolean isEnableTracker() {
        return mEnableTracker;
    }

    public void registerSuperProperties(JSONObject superProperties) {
        mMixpanel.registerSuperProperties(superProperties);
    }

    public void unregisterSuperProperties(String superPropertyName) {
        mMixpanel.unregisterSuperProperty(superPropertyName);
    }

    public void flush() {
        mMixpanel.flush();
    }
}
