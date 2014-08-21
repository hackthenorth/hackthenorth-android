package com.hackthenorth.android.base;

import android.app.Activity;
import android.app.Fragment;
import android.os.AsyncTask;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

public abstract class BaseListFragment extends Fragment {

    /**
     * Decode the json data and make any necessary adjustments to the internal data list.
     * Do not call notifyDataSetChanged on the adapter.
     *
     * @param json json to decode
     */
    abstract protected void onUpdate(String json);

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
