package com.hackthenorth.android.base;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.concurrent.ConcurrentHashMap;

public abstract class BaseListFragment extends Fragment {

    private static final String TAG = "BaseListFragment";

    private static ConcurrentHashMap<String, Object> mMemoryCache =
            new ConcurrentHashMap<String, Object>();

    private BroadcastReceiver mReceiver;
    private Context mContext;

    public static Object getCachedObject(String f) {
        return mMemoryCache.get(f);
    }

    public static void setCachedObject(String f, Object o) {
        mMemoryCache.put(f, o);
    }

    protected abstract void handleJSONUpdateInBackground(String json, String action);

    /**
     * Given an action string, the handleJSONUpdateInBackground method will be called whenever there
     * is an update.
     *
     * @param context The current Fragment context.
     * @param action The action to sync to. This is the same action string you would pass to
     *               HTTPFirebase.GET().
     */
    protected void registerForSync(Context context, final String action) {

        final String className = getClass().getSimpleName();
        final String TAG = className;

        if (mReceiver == null) {
            // Set up BroadcastReceiver for updates.
            mContext = context;
            mReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (action != null && action.equals(intent.getAction())) {

                        // Update with the new data
                        String key = intent.getAction();
                        String json = intent.getStringExtra(key);
                        handleJSONUpdateInBackground(json, action);
                    }
                }
            };

            // Register our broadcast receiver.
            IntentFilter filter = new IntentFilter();
            filter.addAction(action);

            LocalBroadcastManager manager = LocalBroadcastManager.getInstance(mContext);
            manager.registerReceiver(mReceiver, filter);
        }
    }
}
