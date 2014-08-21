package com.hackthenorth.android.base;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.ArrayAdapter;

import com.hackthenorth.android.HackTheNorthApplication;

import java.util.ArrayList;

public abstract class BaseListFragment extends Fragment {

    /**
     * Decode the json data and make any necessary adjustments to the internal data list.
     * Do not call notifyDataSetChanged on the adapter.
     *
     * @param json json to decode
     */
    abstract protected void onUpdate(String json);

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
                    handleJSONInBackground(json, adapter);
                }
            }
        };

        // Register our broadcast receiver.
        IntentFilter filter = new IntentFilter();
        filter.addAction(action);

        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(context);
        manager.registerReceiver(receiver, filter);

    }

    protected void handleJSONInBackground(String json, final ArrayAdapter adapter) {
        final Activity activity = getActivity();
        new AsyncTask<String, Void, Void>(){
            @Override
            protected Void doInBackground(String... strings) {
                String json = strings[0];

                onUpdate(json);

                if (activity != null && adapter != null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                        }
                    });
                }

                return null;
            }
        }.execute(json);
    }
}
