package com.hackthenorth.android;

import android.app.Application;

public class HackTheNorthApplication extends Application {

    // True if one of our activities are visible, false otherwise.
    private static boolean mActivityVisible;

    public static final int NOTIFICATIONS_ID = 1;

    public static interface Actions {
        public final static String SYNC_UPDATES =
                "com.hackthenorth.android.intent.action.SYNC_UPDATES";
    }

    public static void activityResumed() {
        mActivityVisible = true;
    }

    public static void activityPaused() {
        mActivityVisible = false;
    }

    public static boolean isActivityVisible() {
        return mActivityVisible;
    }
}
