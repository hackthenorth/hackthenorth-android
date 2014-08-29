package com.hackthenorth.android.base;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.ArrayAdapter;

import java.util.concurrent.ConcurrentHashMap;

public abstract class BaseListFragment extends Fragment {

    private static final String TAG = "BaseListFragment";

    private static ConcurrentHashMap<String, Object> mMemoryCache =
            new ConcurrentHashMap<String, Object>();

    public static Object getCachedObject(String f) {
        return mMemoryCache.get(f);
    }

    public static void setCachedObject(String f, Object o) {
        mMemoryCache.put(f, o);
    }

    protected abstract void handleJSONUpdateInBackground(String json);

    /**
     * Creates a BroadcastReceiver that is listening to broadcasts of the type action.
     *
     * @param context Used to set up the LocalBroadcastReceiver. Must be non-null.
     * @param action The action to listen to in the broadcast receiver.
     * @param adapter The adapter to pass to handleJSONInBackground in onReceive of the
     *                BroadcastReceiver.
     */
    protected void registerForSync(Context context, final String action,
                                   final ArrayAdapter adapter) {

        // Set up BroadcastReceiver for updates.
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (action != null && action.equals(intent.getAction())) {

                    // Update with the new data
                    String key = intent.getAction();
                    String json = intent.getStringExtra(key);
                    handleJSONUpdateInBackground(json);
                }
            }
        };

        // Register our broadcast receiver.
        IntentFilter filter = new IntentFilter();
        filter.addAction(action);

        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(context);
        manager.registerReceiver(receiver, filter);

    }
}
