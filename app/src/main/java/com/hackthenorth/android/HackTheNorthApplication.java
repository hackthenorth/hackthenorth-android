package com.hackthenorth.android;

import android.app.Application;

import com.hackthenorth.android.framework.NetworkManager;

// TODO: Is there a better place to put these constants?
public class HackTheNorthApplication extends Application {
    public static final int NOTIFICATIONS_ID = 1;

    public static interface Actions {
        public final static String SYNC_UPDATES =
                "com.hackthenorth.android.intent.action.SYNC_UPDATES";
    }

    @Override
    public void onCreate() {
        // Initialize the NetworkManager.
        NetworkManager.initialize(getApplicationContext());
    }
}
