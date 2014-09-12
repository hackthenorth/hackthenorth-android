package com.hackthenorth.android;

import android.app.Application;

import com.hackthenorth.android.framework.NetworkManager;
import com.taplytics.sdk.Taplytics;

// TODO: Is there a better place to put these constants?
public class HackTheNorthApplication extends Application {

    public static interface Actions {
        public final static String SYNC_UPDATES =
                "com.hackthenorth.android.intent.action.SYNC_UPDATES";
        public final static String SYNC_MENTORS =
                "com.hackthenorth.android.intent.action.SYNC_MENTORS";
        public final static String SYNC_SCHEDULE =
                "com.hackthenorth.android.intent.action.SYNC_SCHEDULE";
        public final static String SYNC_PRIZES =
                "com.hackthenorth.android.intent.action.SYNC_PRIZES";
        public final static String SYNC_TEAM =
                "com.hackthenorth.android.intent.action.SYNC_TEAM";
    }

    @Override
    public void onCreate() {
        // Initialize the NetworkManager.
        NetworkManager.initialize(getApplicationContext());
        Taplytics.startTaplytics(this, "c24c6425a9dd30745bf3fbf12dbbfc659c15fff3");

        setTheme(R.style.AppBaseTheme);
    }
}
