
package com.bj4.yhh.utilities.analytics.googleanalytics;

import java.util.HashMap;

import com.bj4.yhh.utilities.SettingManager;
import com.bj4.yhh.utilities.analytics.Analytics;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import android.app.Activity;
import android.content.Context;
import android.os.Build;

public class GoogleAnalyticsTracker {
    private static final String GA_TOKEN = "UA-53591758-1";

    public static class DefaultDimension {
        public static final int BUILD_MODEL = 1;

        public static final int BUILD_FINGERPRINT = 2;

        public static final int BUILD_TYPE = 3;

        public static final int BUILD_DEVICE = 4;

        public static final int PRODUCT_NAME = 5;
    }

    public static class ActivityDimension extends DefaultDimension {
        public static final int BUILD_NUMBER = 6;
    }

    private static GoogleAnalyticsTracker sInstance;

    private boolean mEnableTracker = Analytics.ENABLE_TRACKER;

    public void checkEnableState() {
        mEnableTracker = Analytics.ENABLE_TRACKER;
    }

    public static synchronized GoogleAnalyticsTracker getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new GoogleAnalyticsTracker(context.getApplicationContext());
        }
        return sInstance;
    }

    private static Tracker sTracker;

    private synchronized Tracker getInstanceTracker(Context context) {
        if (sTracker == null) {
            sTracker = GoogleAnalytics.getInstance(context.getApplicationContext()).newTracker(
                    GA_TOKEN);
            sTracker.set(Fields.customDimension(DefaultDimension.BUILD_MODEL), Build.MODEL);
            sTracker.set(Fields.customDimension(DefaultDimension.BUILD_FINGERPRINT),
                    Build.FINGERPRINT);
            sTracker.set(Fields.customDimension(DefaultDimension.BUILD_TYPE), Build.TYPE);
            sTracker.set(Fields.customDimension(DefaultDimension.BUILD_DEVICE), Build.DEVICE);
            sTracker.set(Fields.customDimension(DefaultDimension.PRODUCT_NAME), Build.PRODUCT);
            sTracker.set(Fields.customDimension(ActivityDimension.BUILD_NUMBER),
                    sTracker.get(Fields.CLIENT_ID));
        }
        return sTracker;
    }

    private GoogleAnalyticsTracker(Context context) {
        mEnableTracker = SettingManager.getInstance(context).isEnableGa();
    }

    public void sendException(Context context, String exceptionDescription, boolean fatal,
            HashMap<Integer, String> dimenMap) {
        if (mEnableTracker) {
            Tracker tracker = getInstanceTracker(context);
            MapBuilder mb = MapBuilder.createException(exceptionDescription, fatal);
            tracker.send(mb.build());
        }
    }

    public void sendEvents(Context context, String category, String action, String label, Long value) {
        if (mEnableTracker) {
            Tracker tracker = getInstanceTracker(context);
            tracker.send(MapBuilder.createEvent(category, action, label, value).build());
        }
    }

    public void sendTiming(Context context, String category, long intervalInMilliseconds,
            String name, String label) {
        if (mEnableTracker) {
            Tracker tracker = getInstanceTracker(context);
            tracker.send(MapBuilder.createTiming(category, intervalInMilliseconds, name, label)
                    .build());
        }
    }

    public void sendView(Context context, String appScreen, HashMap<Integer, String> dimenMap) {
        if (mEnableTracker) {
            Tracker tracker = getInstanceTracker(context);
            tracker.set(Fields.SCREEN_NAME, appScreen);
            MapBuilder mb = MapBuilder.createAppView();
            StringBuilder sb = new StringBuilder();
            tracker.send(mb.build());
        }
    }

    public void activityStart(Activity activity) {
        if (mEnableTracker) {
            EasyTracker.getInstance(activity).activityStart(activity);
        }
    }

    public void activityStop(Activity activity) {
        if (mEnableTracker) {
            EasyTracker.getInstance(activity).activityStop(activity);
        }
    }
}
