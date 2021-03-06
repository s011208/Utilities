
package com.bj4.yhh.utilities.analytics.flurry;

import java.util.HashMap;

import com.bj4.yhh.utilities.SettingManager;
import com.bj4.yhh.utilities.analytics.Analytics;
import com.flurry.android.FlurryAgent;

import android.content.Context;
import android.util.Log;

public class FlurryTracker {
    private static final String FLURRY_APPLICATION_KEY = "MWRQJK4WP7HBKKPKZBXT";

    private static FlurryTracker sInstance;

    public synchronized static FlurryTracker getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new FlurryTracker(context);
        }
        return sInstance;
    }

    private boolean mEnableTracker = Analytics.ENABLE_TRACKER;

    private FlurryTracker(Context context) {
        mEnableTracker = SettingManager.getInstance(context).isEnableGa();
        FlurryAgent.setLogLevel(Log.VERBOSE);
    }

    public void checkEnableState() {
        mEnableTracker = Analytics.ENABLE_TRACKER;
    }

    public void track(String eventName) {
        if (!mEnableTracker)
            return;
        FlurryAgent.logEvent(eventName);
    }

    public void track(String eventName, HashMap<String, String> properties) {
        if (!mEnableTracker)
            return;
        FlurryAgent.logEvent(eventName, properties);
    }

    public static void startSession(Context context) {
        FlurryAgent.onStartSession(context, FLURRY_APPLICATION_KEY);
    }

    public static void endSession(Context context) {
        FlurryAgent.onEndSession(context);
    }
}
